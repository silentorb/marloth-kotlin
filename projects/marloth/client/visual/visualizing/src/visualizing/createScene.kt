package visualizing

import mythic.spatial.*
import org.joml.times
import scenery.*
import org.joml.plus
import org.joml.unaryMinus
import physics.Body
import scenery.Id
import simulation.*

val firstPersonCameraOffset = Vector3(0f, 0f, 1.4f)

fun createFirstPersonCamera(character: Character): Camera = Camera(
    ProjectionType.perspective,
    character.body.position + firstPersonCameraOffset,
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
  val body = world.bodyTable [player.character]!!
  return when (player.viewMode) {
    ViewMode.firstPerson -> createFirstPersonCamera(character)
    ViewMode.thirdPerson -> createThirdPersonCamera(body, player.hoverCamera)
//    ViewMode.topDown -> createTopDownCamera(body)
  }
}

fun filterDepictions(world: World, player: Player) =
    if (player.viewMode == ViewMode.firstPerson)
      world.depictionTable.filter { it.key != player.playerId }
    else
      world.depictionTable

fun convertDepiction(world: World, id: Id, depiction: Depiction): VisualElement {
  val body = world.bodyTable[id]!!
  val character = world.characterTable[id]
  val translate = Matrix().translate(body.position)
  val transform = if (character != null)
    translate.rotate(character.facingQuaternion)
  else
    translate.rotate(body.orientation)

  return VisualElement(id, depiction.type, depiction.animation, transform)
}

fun mapElements(world: World, screen: Screen, player: Player): Pair<List<VisualElement>, ElementDetails> {
  val depictions = filterDepictions(world, player)
  val childDepictions = depictions.values.filter {
    it.type == DepictionType.character || it.type == DepictionType.monster
  }
//  val billboardDepictions = depictions.values.filter {
//    it.type == DepictionType.billboard
//  }
//  val elements = childDepictions.map {
//    convertDepiction(world, it.id, it)
//  }
//      .plus(otherDepictions.map {
//        convertDepiction(world, it.id, it)
//      })
  val elements = depictions.values.map {
    convertDepiction(world, it.id, it)
  }

  return Pair(
      elements,
      ElementDetails(
          children = childDepictions.associate { Pair(it.id, ChildDetails(if (it.type == DepictionType.character) Gender.female else Gender.male)) }
//          billboards = world.billboardTable.mapValues { BillboardDetails(it.value.text) }
      )
  )
}

fun createScene(world: World, screen: Screen, player: Player): GameScene {
  val (elements, elementDetails) = mapElements(world, screen, player)
  val body = world.bodyTable [player.character]!!
  return GameScene(
      main = Scene(
          createCamera(world, screen),
          world.lights.values.plus(Light(
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