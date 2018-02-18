package simulation

enum class ViewMode {
  firstPerson,
  topDown,
  thirdPerson
}

class Player(
    val character: Character,
    val playerId: Int,
    var viewMode: ViewMode
) {
}

fun isPlayer(world: World, character: Character) =
    world.players.any { it.playerId == character.id }
