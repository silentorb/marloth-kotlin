package simulation

import mythic.spatial.Vector2

enum class ViewMode {
  firstPerson,
//  topDown
  thirdPerson
}

data class HoverCamera(
    var pitch: Float = -0.4f,
    var yaw: Float = 0f,
    var distance: Float = 7f
)

class Player(
    val character: Character,
    val playerId: Int,
    var viewMode: ViewMode,
    var lookForce: Vector2 = Vector2(),
    var lookVelocity: Vector2 = Vector2(),
    var hoverCamera: HoverCamera = HoverCamera()
)

fun isPlayer(world: World, character: Character) =
    world.players.any { it.playerId == character.id }
