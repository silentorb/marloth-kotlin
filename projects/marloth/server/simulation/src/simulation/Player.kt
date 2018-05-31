package simulation

import mythic.spatial.Vector3

enum class ViewMode {
  firstPerson,
  topDown,
  thirdPerson
}

data class HoverCamera(
    var pitch: Float = -0.4f,
    var yaw: Float = 0f,
    var distance: Float = 8f
)

class Player(
    val character: Character,
    val playerId: Int,
    var viewMode: ViewMode,
    var lookVelocity: Vector3 = Vector3(),
    var hoverCamera: HoverCamera = HoverCamera()
)

fun isPlayer(world: World, character: Character) =
    world.players.any { it.playerId == character.id }
