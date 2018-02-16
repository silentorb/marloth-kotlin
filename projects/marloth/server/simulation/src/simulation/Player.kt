package simulation

class Player(
    val character: Character,
    val playerId: Int
) {
}

fun isPlayer(world: World, character: Character) =
    world.players.any { it.playerId == character.id }
