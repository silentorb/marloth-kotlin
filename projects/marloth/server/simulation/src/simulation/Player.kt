package simulation

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

data class Player(
    val character: Id,
    val playerId: Int,
    val viewMode: ViewMode,
    val hoverCamera: HoverCamera = HoverCamera()
)

data class PlayerCharacter(
    val player: Player,
    val character: Character
)

typealias PlayerCharacters = List<PlayerCharacter>

fun isPlayer(world: WorldMap, character: Character) =
    world.players.any { it.playerId == character.id }
