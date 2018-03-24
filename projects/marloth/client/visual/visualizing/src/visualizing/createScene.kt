package visualizing

import mythic.spatial.*
import org.joml.times
import scenery.*
import org.joml.plus
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

fun createThirdPersonCamera(player: Body): Camera = Camera(
    ProjectionType.perspective,
    player.position,
//    Quaternion(0f, 0f, 0.1f),
    Quaternion(),
    45f
)

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
  val body = player.character.body
  return when (player.viewMode) {
    ViewMode.firstPerson -> createFirstPersonCamera(player.character)
    ViewMode.thirdPerson -> createThirdPersonCamera(body)
    ViewMode.topDown -> createTopDownCamera(body)
  }
}

//val depictionMap = mapOf(
//    EntityType.character to DepictionType.character,
//    EntityType.missile to DepictionType.missile,
//    EntityType.monster to DepictionType.monster
//)

//fun prepareVisualElement(body: Body, entityType: EntityType): VisualElement? {
//  val depiction = depictionMap[entityType]
//  return if (depiction != null)
//    VisualElement(depiction, Matrix().translate(body.position))
//  else
//    null
//}

//fun selectBodies(world: World, player: Player) =

//    world.bodies
//        .filter { player.viewMode != ViewMode.firstPerson || it !== player.character.body }
//        .mapNotNull { prepareVisualElement(it, world.entities[it.id]!!.type) }

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
    translate

  return VisualElement(depiction.type, transform)
}

fun createScene(world: World, screen: Screen, player: Player) =
    GameScene(
        Scene(
            createCamera(world, screen),
            world.lights.plus(Light(
                type = LightType.point,
                color = Vector4(1f, 1f, 1f, 1f),
                position = player.character.body.position + Vector3(0f, 0f, 2f),
                direction = Vector4(0f, 0f, 0f, 15f)
            ))
        ),
        filterDepictions(world, player)
            .map {
              convertDepiction(world, it.key, it.value)
//              val body = world.bodyTable[it.key]!!
//              val character = player.character
//              val transform = Matrix().translate(body.position)
//                  .rotate(character.facingQuaternion)
//              VisualElement(it.value.type, transform)
            },
        player.playerId

    )

fun createScenes(world: World, screens: List<Screen>) =
    world.players.map {
      createScene(world, screens[it.playerId - 1], it)
    }