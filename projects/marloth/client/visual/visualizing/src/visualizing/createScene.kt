package visualizing

import org.joml.times
import scenery.*
import mythic.spatial.Matrix
import mythic.spatial.Pi
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import org.joml.plus
import simulation.*

fun createFirstPersonCamera(character: Character): Camera = Camera(
    character.body.position + Vector3(0f, 0f, 0.4f),
//    world.player.orientation,
    character.facingQuaternion
    ,
    45f
)

fun createThirdPersonCamera(player: Body): Camera = Camera(
    player.position,
    Quaternion(0f, 0f, .1f),
    45f
)

fun createOrthographicCamera(player: Body): Camera {
  val position = Vector3(0f, -20f, 20f) + player.position
  return Camera(
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
    ViewMode.topDown -> createOrthographicCamera(body)
  }
}

val depictionMap = mapOf(
    EntityType.character to DepictionType.character,
    EntityType.missile to DepictionType.missile
)

fun prepareVisualElement(body: Body, entityType: EntityType): VisualElement? {
  val depiction = depictionMap[entityType]
  return if (depiction != null)
    VisualElement(depiction, Matrix().translate(body.position))
  else
    null
}

fun selectBodies(world: World, player: Player) =
    world.bodies
        .filter { player.viewMode != ViewMode.firstPerson || it !== player.character.body }
        .mapNotNull { prepareVisualElement(it, world.entities[it.id]!!.type) }

fun createScene(world: World, screen: Screen, player: Player) =
    Scene(
        createCamera(world, screen),
        selectBodies(world, player),
        player.playerId,
        world.lights
    )

fun createScenes(world: World, screens: List<Screen>) =
    world.players.map {
      createScene(world, screens[it.playerId - 1], it)
    }