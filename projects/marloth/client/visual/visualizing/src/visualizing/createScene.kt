package visualizing

import org.joml.times
import scenery.*
import simulation.Player
import simulation.World
import mythic.spatial.Matrix
import mythic.spatial.Pi
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import org.joml.plus
import simulation.Body
import simulation.EntityType

fun createFirstPersonCamera(player: Body): Camera = Camera(
    player.position,
//    world.player.orientation,
    Quaternion(0f, 0f, .1f),
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
  val body = world.bodyTable[player.character.id]!!
  return when (screen.cameraMode) {
    CameraMode.firstPerson -> createFirstPersonCamera(body)
    CameraMode.thirdPerson -> createThirdPersonCamera(body)
    CameraMode.topDown -> createOrthographicCamera(body)
  }
}

val depictionMap = mapOf(
    EntityType.character to Depiction.character,
    EntityType.missile to Depiction.missile
)

fun prepareVisualElement(body: Body, entityType: EntityType): VisualElement? {
  val depiction = depictionMap[entityType]
  return if (depiction != null)
    VisualElement(depiction, Matrix().translate(body.position))
  else
    null
}

fun createScene(world: World, screen: Screen, player: Player) =
    Scene(
        createCamera(world, screen),
        world.bodies
            .mapNotNull { prepareVisualElement(it, world.entities[it.id]!!.type) },
        player.playerId
    )

fun createScenes(world: World, screens: List<Screen>) =
    world.players.map {
      createScene(world, screens[it.playerId - 1], it)
    }