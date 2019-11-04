package marloth.integration

import scenery.enums.MeshId
import mythic.ent.Id
import mythic.ent.Table
import mythic.spatial.*
import org.joml.times
import simulation.physics.Body
import rendering.*
import scenery.*
import scenery.Light
import scenery.enums.AccessoryId
import simulation.entities.*
import simulation.main.Deck
import simulation.main.defaultPlayer
import simulation.physics.defaultCharacterHeight
import kotlin.math.floor

val firstPersonCameraOffset = Vector3(0f, 0f, defaultCharacterHeight / 2f)
val firstPersonDeadCameraOffset = Vector3(0f, 0f, -0.75f)

val simplePainterMap = MeshId.values().mapNotNull { meshId ->
  val depictionType = DepictionType.values().firstOrNull { it.name == meshId.name }
  if (depictionType != null)
    Pair(depictionType, meshId)
  else
    null
}.associate { it }
    .plus(
        mapOf(
            DepictionType.child to MeshId.personBody
        )
    )

fun firstPersonCamera(body: Body, character: Character, isAlive: Boolean): Camera = Camera(
    ProjectionType.perspective,
//    body.position + Vector3(0f, 3f, -0.75f), //if (isAlive) firstPersonCameraOffset else firstPersonDeadCameraOffset,
    body.position + if (isAlive) firstPersonCameraOffset else firstPersonDeadCameraOffset,
//    world.player.orientation,
    if (isAlive) character.facingQuaternion else character.facingQuaternion * Quaternion().rotateX(Pi / -6f),
    45f
)

fun thirdPersonCamera(body: Body, hoverCamera: HoverCamera): Camera {
  val orientation = Quaternion()
      .rotateZ(hoverCamera.yaw)
      .rotateY(hoverCamera.pitch)

  val offset = orientation * Vector3(hoverCamera.distance, 0f, 0f)
  val orientationSecond = Quaternion().rotateTo(Vector3(1f, 0f, 0f), -offset)
  val position = offset + body.position + Vector3(0f, 0f, 1f)
  return Camera(ProjectionType.perspective, position, orientationSecond, 45f)
}

fun createTopDownCamera(player: Body): Camera {
  val position = Vector3(0f, -20f, 20f) + player.position
  return Camera(
      ProjectionType.perspective,
      position,
      Quaternion().rotate(0f, 0f, Pi * 0.5f)
          *
          Quaternion().rotate(0f, Pi * 0.25f, 0f)
      ,
      45f
  )
}

fun createCamera(deck: Deck, screen: Screen): Camera {
  val player = defaultPlayer(deck)
  val character = deck.characters[player]!!
  val body = deck.bodies[player]!!
  val playerRecord = deck.players[player]!!
  return when (playerRecord.viewMode) {
    ViewMode.firstPerson -> firstPersonCamera(body, character, character.isAlive)
    ViewMode.thirdPerson -> thirdPersonCamera(body, playerRecord.hoverCamera)
//    ViewMode.topDown -> createTopDownCamera(body)
  }
}

fun filterDepictions(depictions: Table<Depiction>, player: Id, playerRecord: Player): Table<Depiction> =
    if (playerRecord.viewMode == ViewMode.firstPerson)
      depictions.filter { it.key != player }
    else
      depictions

fun convertSimpleDepiction(deck: Deck, id: Id, mesh: MeshName, texture: TextureName? = null): MeshElement? {
  val body = deck.bodies[id]!!
  val character = deck.characters[id]
  val translate = Matrix().translate(body.position)
  val transform = if (character != null)
    translate.rotate(character.facingQuaternion)
  else
    translate.rotate(body.orientation)

  val material = if (texture != null)
    Material(texture = texture, shading = true)
  else
    null

  return MeshElement(
      id = id,
      mesh = mesh,
      transform = transform.scale(body.scale),
      material = material
  )
}

fun convertSimpleDepiction(deck: Deck, id: Id, depiction: Depiction): MeshElement? {
  val mesh = if (depiction.type == DepictionType.staticMesh)
    depiction.mesh
  else
    simplePainterMap[depiction.type]?.name

  if (mesh == null)
    return null

  return convertSimpleDepiction(deck, id, mesh, depiction.texture)
}

fun convertComplexDepiction(deck: Deck, id: Id, depiction: Depiction): ElementGroup {
  val body = deck.bodies[id]!!
  val character = deck.characters[id]!!
  val collisionObject = deck.collisionShapes[id]!!
  val shape = collisionObject.shape
  val verticalOffset = -shape.height / 2f

  val transform = Matrix()
      .translate(body.position + Vector3(0f, 0f, verticalOffset))
      .rotate(character.facingQuaternion)
      .rotateZ(Pi / 2f)
//      .scale(1.5f)

  val animations = depiction.animations.map {
    ElementAnimation(
        animationId = mapAnimation(deck, it.animationId),
        timeOffset = it.animationOffset,
        strength = it.strength
//        strength = 1f / depiction.animations.size
    )
  }
  val meshes = when {
    depiction.type == DepictionType.child -> listOf(
        MeshId.personBody,
        MeshId.pants,
        MeshId.shirt,
        MeshId.pumpkinHead
    )
    else -> listOf(
        MeshId.personBody,
        MeshId.hogHead,
        MeshId.pants,
        MeshId.shirt
    )
  }

  return ElementGroup(
      meshes = meshes
          .map {
            MeshElement(
                id = id,
                mesh = it.name,
                transform = transform
            )
          },
      armature = ArmatureId.person,
      animations = animations,
      attachments = listOf(
          AttachedMesh(
              socket = ArmatureSockets.rightHand.toString(),
              mesh = MeshElement(
                  id = id,
                  mesh = MeshId.pistol.name,
                  transform = transform
              )
          )
      )
  )
}

private val complexDepictions = setOf(
    DepictionType.child,
    DepictionType.person
)

val isComplexDepiction = { depiction: Depiction ->
  complexDepictions.contains(depiction.type)
}

fun gatherParticleElements(deck: Deck, cameraPosition: Vector3): ElementGroups {
  return deck.particleEffects
      .entries.sortedByDescending { cameraPosition.distance(deck.bodies[it.key]!!.position) }
      .map { (_, particleEffect) ->
        ElementGroup(
            billboards = particleEffect.particles.map { particle ->
              TexturedBillboard(
                  texture = particle.texture,
                  position = particle.position,
                  scale = particle.size,
                  color = particle.color,
                  step = floor(particle.animationStep * 16f).toInt()
              )
            }
        )
      }
}

fun gatherVisualElements(deck: Deck, screen: Screen, player: Id, playerRecord: Player): ElementGroups {
  val (complex, simple) =
      filterDepictions(deck.depictions, player, playerRecord)
          .entries.partition { isComplexDepiction(it.value) }

  val complexElements = complex.map {
    convertComplexDepiction(deck, it.key, it.value)
  }

  val simpleElements =
      simple.mapNotNull {
        convertSimpleDepiction(deck, it.key, it.value)
      }
          .plus(deck.doors.mapNotNull {
            convertSimpleDepiction(deck, it.key, MeshId.prisonDoor.name)
          })

  return complexElements
      .plus(ElementGroup(simpleElements))
}

fun mapLights(deck: Deck, player: Id) =
    deck.lights
        .map { (id, light) ->
          Light(
              type = LightType.point,
              color = light.color,
              position = deck.bodies[id]!!.position + Vector3(0f, 0f, 2.2f),
              direction = Vector3(0f, 0f, 0f),
              range = light.range
          )
        }
        .plus(listOfNotNull(
            if (hasEquipped(deck, player)(AccessoryId.candle))
              Light(
                  type = LightType.point,
                  color = Vector4(1f, 1f, 1f, 0.6f),
                  position = deck.bodies[player]!!.position + Vector3(0f, 0f, 2f),
                  direction = Vector3(0f, 0f, 0f),
                  range = 36f
              )
            else
              null
        ))

fun createBackgroundSphere(texture: BackgroundTextureId, cameraPosition: Vector3, orientation: Quaternion = Quaternion()) =
    MeshElement(
        id = 1,
        mesh = MeshId.skySphere.toString(),
        transform = Matrix()
            .translate(cameraPosition)
            .rotate(orientation)
            .scale(100f),
        material = Material(
            color = Vector4(1f, 1f, 1f, 1f),
            texture = texture.name,
            shading = false
        )
    )

fun gatherBackground(cycles: Table<Cycle>, cameraPosition: Vector3): ElementGroups {
  return listOf(ElementGroup(
      meshes = listOf(
          Pair(BackgroundTextureId.backgroundNightSky, Quaternion()),
          Pair(BackgroundTextureId.backgroundClouds, Quaternion().rotateZ(cycles.values.first().value * Pi * 2f)),
          Pair(BackgroundTextureId.backgroundClouds, Quaternion()
              .rotateX(Pi)
              .rotateZ(cycles.values.drop(1).first().value * Pi * 2f))

      ).map { createBackgroundSphere(it.first, cameraPosition, it.second) }
  ))
}

fun createScene(deck: Deck, screen: Screen, player: Id): GameScene {
  val camera = createCamera(deck, screen)
  return GameScene(
      main = Scene(
          camera = camera,
          lights = mapLights(deck, player)
      ),
      opaqueElementGroups = gatherVisualElements(deck, screen, player, deck.players[player]!!),
      transparentElementGroups = gatherParticleElements(deck, camera.position),
      filters = if (!deck.characters[player]!!.isAlive)
        listOf<ScreenFilter>(
            { it.screenDesaturation.activate() },
            { it.screenColor.activate(Vector4(1f, 0f, 0f, 0.4f)) }
        )
      else
        listOf(),
      background = gatherBackground(deck.cycles, camera.position)
  )
}

fun createScenes(deck: Deck, screens: List<Screen>): List<GameScene> =
    deck.players.keys.mapIndexed() { i, key ->
      createScene(deck, screens[i], key)
    }
