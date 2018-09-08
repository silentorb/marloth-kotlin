package visualizing

import mythic.spatial.*
import org.joml.times
import physics.Body
import rendering.GameScene
import rendering.MeshElement
import rendering.MeshType
import scenery.*
import simulation.*
import simulation.Id

val firstPersonCameraOffset = Vector3(0f, 0f, 1.4f)

val simplePainterMap = mapOf(
    DepictionType.monster to MeshType.eyeball,
    DepictionType.character to MeshType.child,
    DepictionType.missile to MeshType.sphere,
    DepictionType.wallLamp to MeshType.wallLamp
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

//fun filterDepictions(world: World, player: Player) =
//    if (player.viewMode == ViewMode.firstPerson)
//      world.depictionTable.filter { it.key != player.playerId }
//    else
//      world.depictionTable

fun convertDepiction(world: World, id: Id, type: DepictionType): MeshElement? {
  val mesh = simplePainterMap[type]
  if (mesh == null)
    return null

  val body = world.bodyTable[id]!!
  val character = world.characterTable[id]
  val translate = Matrix().translate(body.position)
  val transform = if (character != null)
    translate.rotate(character.facingQuaternion)
  else
    translate.rotate(body.orientation)

//  val animation = if (depiction != null)
//    depiction.animation
//  else
//    null
  return MeshElement(
      id = id,
      mesh = mesh,
//      null,
      transform = transform
  )
}

fun createChildDetails(character: Character): ChildDetails =
    ChildDetails(if (character.definition.depictionType == DepictionType.character) Gender.female else Gender.male)

fun gatherVisualElements(world: World, screen: Screen, player: Player): Pair<List<MeshElement>, ElementDetails> {
//  val depictions = filterDepictions(world, player)
//  val childDepictions = depictions.values.filter {
//    it.type == DepictionType.character || it.type == DepictionType.monster
//  }

  val characters = world.characters.asIterable().filter { !isPlayer(world, it) }
  val elements =
      world.deck.depictions.mapNotNull {
        convertDepiction(world, it.id, it.type)
      }
          .plus(characters.mapNotNull {
            convertDepiction(world, it.id, it.definition.depictionType)
          })
          .plus(world.missileTable.values.mapNotNull {
            convertDepiction(world, it.id, DepictionType.missile)
          })

  return Pair(
      elements,
      ElementDetails(
          children = characters.associate { Pair(it.id, createChildDetails(it)) }
      )
  )
}

fun createScene(world: World, screen: Screen, player: Player): GameScene {
  val (elements, elementDetails) = gatherVisualElements(world, screen, player)
  val body = world.bodyTable[player.character]!!
  return GameScene(
      main = Scene(
          createCamera(world, screen),
          world.deck.depictions.filter { it.type == DepictionType.wallLamp }
              .map {
                Light(
                    type = LightType.point,
                    color = Vector4(1f, 1f, 1f, 1f),
                    position = body.position + Vector3(0f, 0f, 2f),
                    direction = Vector4(0f, 0f, 0f, 15f)
                )
              }
              .plus(Light(
                  type = LightType.point,
                  color = Vector4(1f, 1f, 1f, 1f),
                  position = body.position + Vector3(0f, 0f, 2f),
                  direction = Vector4(0f, 0f, 0f, 15f)
              ))
      ),
      elements = elements,
      elementDetails = elementDetails,
      player = player.playerId
  )
}

fun createScenes(world: World, screens: List<Screen>) =
    world.players.map {
      createScene(world, screens[it.playerId - 1], it)
    }