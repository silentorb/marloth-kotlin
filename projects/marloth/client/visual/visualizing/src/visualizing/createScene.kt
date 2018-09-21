package visualizing

import mythic.spatial.*
import org.joml.times
import physics.Body
import rendering.*
import scenery.*
import simulation.*
import simulation.Id

val firstPersonCameraOffset = Vector3(0f, 0f, 1.4f)

val simplePainterMap = mapOf(
    DepictionType.monster to MeshId.eyeball,
    DepictionType.child to MeshId.childBody,
    DepictionType.missile to MeshId.sphere,
    DepictionType.wallLamp to MeshId.wallLamp
)

fun createFirstPersonCamera(body: Body, character: Character): Camera = Camera(
    ProjectionType.perspective,
    body.position + firstPersonCameraOffset,
//    world.player.orientation,
    character.facingQuaternion
    ,
    45f
)

fun createThirdPersonCamera(body: Body, hoverCamera: HoverCamera): Camera {
  val orientation = Quaternion()
      .rotateZ(hoverCamera.yaw)
      .rotateY(hoverCamera.pitch)

  val offset = orientation * Vector3(hoverCamera.distance, 0f, 0f)
  val orientationSecond = Quaternion().rotateTo(Vector3(1f, 0f, 0f), -offset)
  val position = offset + body.position + Vector3(0f, 0f, 1f)
  return Camera(ProjectionType.perspective, position, orientationSecond, 45f)
//  return Camera(
//      ProjectionType.perspective,
//      body.position,
//      Quaternion()
//          .rotateZ(hoverCamera.yaw)
//          .rotateY(hoverCamera.pitch),
//      45f
//  )
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
  val character = world.characterTable[player.character]!!
  val body = world.bodyTable[player.character]!!
  return when (player.viewMode) {
    ViewMode.firstPerson -> createFirstPersonCamera(body, character)
    ViewMode.thirdPerson -> createThirdPersonCamera(body, player.hoverCamera)
//    ViewMode.topDown -> createTopDownCamera(body)
  }
}

fun filterDepictions(depictions: List<Depiction>, player: Player) =
    if (player.viewMode == ViewMode.firstPerson)
      depictions.filter { it.id != player.character }
    else
      depictions

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
      .scale(2f)

  val firstAnimation = depiction.animations.first()
  val animations = listOf(
      ElementAnimation(
          animation = firstAnimation.animation,
          timeOffset = firstAnimation.animationOffset
      )
  )
  val commonMeshes = listOf(
      MeshId.childBody,
      MeshId.childEyes
  )
  val girlMeshes = listOf(
      MeshId.childGown,
      MeshId.childLongHair
  )

  val meshes = commonMeshes.plus(girlMeshes)

  return ElementGroup(
      meshes = meshes.map {
        MeshElement(
            id = 0,
            mesh = it,
            transform = transform
        )
      },
      armature = ArmatureId.child,
      animations = animations
  )
}

val isComplexDepiction = { depiction: Depiction ->
  depiction.type == DepictionType.child
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
//          .plus(characters.mapNotNull {
//            convertSimpleDepiction(world, it.id, it.definition.depictionType)
//          })
          .plus(world.deck.missiles.mapNotNull {
            convertSimpleDepiction(world, it.id, MeshId.missile)
          })
          .plus(world.deck.doors.mapNotNull {
            convertSimpleDepiction(world, it.id, MeshId.prisonDoor)
          })

  return complexElements.plus(ElementGroup(simpleElements))
}

fun mapLights(world: World, player: Player) =
    world.deck.depictions.filter { it.type == DepictionType.wallLamp }
        .map {
          Light(
              type = LightType.point,
              color = Vector4(1f, 1f, 1f, 1f),
              position = world.bodyTable[it.id]!!.position + Vector3(0f, 0f, 1.6f),
              direction = Vector4(0f, 0f, 0f, 15f)
          )
        }
        .plus(Light(
            type = LightType.point,
            color = Vector4(1f, 1f, 1f, 1f),
            position = world.bodyTable[player.character]!!.position + Vector3(0f, 0f, 2f),
            direction = Vector4(0f, 0f, 0f, 15f)
        ))

fun createScene(world: World, screen: Screen, player: Player): GameScene {
  val elementGroups = gatherVisualElements(world, screen, player)
  return GameScene(
      main = Scene(
          camera = createCamera(world, screen),
          lights = mapLights(world, player)
      ),
      elementGroups = elementGroups,
      player = player.playerId
  )
}

fun createScenes(world: World, screens: List<Screen>) =
    world.players.map {
      createScene(world, screens[it.playerId - 1], it)
    }