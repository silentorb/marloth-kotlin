package marloth.integration

import mythic.spatial.*
import org.joml.times
import physics.Body
import rendering.*
import scenery.*
import simulation.*
import mythic.ent.Id
import mythic.ent.Table
import scenery.Light

val firstPersonCameraOffset = Vector3(0f, 0f, 1.4f)
val firstPersonDeadCameraOffset = Vector3(0f, 0f, 0.4f)

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

fun createCamera(world: World, screen: Screen): Camera {
  val player = world.players[screen.playerId - 1]
  val character = world.characterTable[player.id]!!
  val body = world.bodyTable[player.id]!!
  return when (player.viewMode) {
    ViewMode.firstPerson -> firstPersonCamera(body, character, character.isAlive)
    ViewMode.thirdPerson -> thirdPersonCamera(body, player.hoverCamera)
//    ViewMode.topDown -> createTopDownCamera(body)
  }
}

fun filterDepictions(depictions: Table<Depiction>, player: Player): Collection<Depiction> =
    if (player.viewMode == ViewMode.firstPerson)
      depictions.values.filter { it.id != player.id }
    else
      depictions.values

fun convertSimpleDepiction(world: World, id: Id, mesh: MeshId): MeshElement? {
  val body = world.bodyTable[id]!!
  val character = world.characterTable[id]
  val translate = Matrix().translate(body.position)
  val transform = if (character != null)
    translate.rotate(character.facingQuaternion)
  else
    translate.rotate(body.orientation)

  return MeshElement(
      id = id,
      mesh = mesh,
      transform = transform
  )
}

fun convertSimpleDepiction(world: World, id: Id, type: DepictionType): MeshElement? {
  val mesh = simplePainterMap[type]
  if (mesh == null)
    return null

  return convertSimpleDepiction(world, id, mesh)
}

fun convertComplexDepiction(world: World, depiction: Depiction): ElementGroup {
  val id = depiction.id
  val body = world.bodyTable[id]!!
  val character = world.characterTable[id]!!
  val transform = Matrix()
      .translate(body.position)
      .rotate(character.facingQuaternion)
      .rotateZ(Pi / 2f)
      .scale(1.75f)

  val animations = depiction.animations.map {
    ElementAnimation(
        animationId = mapAnimation(world, it.animationId),
        timeOffset = it.animationOffset,
        strength = it.strength
//        strength = 1f / depiction.animations.size
    )
  }

  return if (depiction.type == DepictionType.child) {
    val commonMeshes = listOf(
        MeshId.personBody
    )
    val girlMeshes = listOf<MeshId>(
//        MeshId.childGown,
//        MeshId.childLongHair
    )

    val meshes = commonMeshes.plus(girlMeshes)

    ElementGroup(
        meshes = meshes.map {
          MeshElement(
              id = 0,
              mesh = it,
              transform = transform
          )
        },
        armature = ArmatureId.person,
        animations = animations
    )
  } else {
    ElementGroup(
        meshes = listOf(
            MeshId.personBody,
            MeshId.hogHead
        )
            .map {
              MeshElement(
                  id = id,
                  mesh = it,
                  transform = transform.scale(0.75f)
              )
            },
        armature = ArmatureId.person,
        animations = animations
    )
//        armature = ArmatureId.child,
//        animations = animations
  }
}

private val complexDepictions = setOf(
//    DepictionType.child,
    DepictionType.person
)

val isComplexDepiction = { depiction: Depiction ->
  complexDepictions.contains(depiction.type)
}

fun gatherVisualElements(world: World, screen: Screen, player: Player): ElementGroups {
//  val depictions = filterDepictions(world, player)
//  val childDepictions = depictions.values.filter {
//    it.type == DepictionType.child || it.type == DepictionType.monster
//  }

  val (complex, simple) = filterDepictions(world.deck.depictions, player).partition(isComplexDepiction)

  val complexElements = complex.map {
    convertComplexDepiction(world, it)
  }

//  val characters = world.characters.asIterable().filter { !isPlayer(world, it) }
  val simpleElements =
      simple.mapNotNull {
        convertSimpleDepiction(world, it.id, it.type)
      }
//          .plusBounded(characters.mapNotNull {
//            convertSimpleDepiction(world, it.id, it.definition.depictionType)
//          })
          .plus(world.deck.missiles.values.mapNotNull {
            convertSimpleDepiction(world, it.id, MeshId.spikyBall)
          })
          .plus(world.deck.doors.values.mapNotNull {
            convertSimpleDepiction(world, it.id, MeshId.prisonDoor)
          })

  return complexElements.plus(ElementGroup(simpleElements))
}

fun mapLights(world: World, player: Player) =
    world.deck.lights.values
        .map { light ->
          Light(
              type = LightType.point,
              color = light.color,
              position = world.bodyTable[light.id]!!.position + Vector3(0f, 0f, 2.2f),
              direction = Vector4(0f, 0f, 0f, light.range)
          )
        }
        .plus(listOfNotNull(
            if (isHolding(world.deck, player.id)(ItemType.candle))
              Light(
                  type = LightType.point,
                  color = Vector4(1f, 1f, 1f, 0.6f),
                  position = world.bodyTable[player.id]!!.position + Vector3(0f, 0f, 2f),
                  direction = Vector4(0f, 0f, 0f, 18f)
              )
            else
              null
        ))

fun createScene(world: World, screen: Screen, player: Player): GameScene {
  val elementGroups = gatherVisualElements(world, screen, player)
  return GameScene(
      main = Scene(
          camera = createCamera(world, screen),
          lights = mapLights(world, player)
      ),
      elementGroups = elementGroups,
      player = player.playerId,
      filters = if (!world.deck.characters[player.id]!!.isAlive)
        listOf<ScreenFilter>(
            { it.screenDesaturation.activate() },
            { it.screenColor.activate(Vector4(1f, 0f, 0f, 0.4f)) }
        )
      else
        listOf()
  )
}

fun createScenes(world: World, screens: List<Screen>): List<GameScene> =
    world.players.map {
      createScene(world, screens[it.playerId - 1], it)
    }