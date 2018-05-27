package simulation

import mythic.spatial.Vector3

enum class ViewMode {
  firstPerson,
  topDown,
  thirdPerson
}

class Player(
    val character: Character,
    val playerId: Int,
    var viewMode: ViewMode,
    var lookVelocity: Vector3 = Vector3()
) {
}

fun isPlayer(world: World, character: Character) =
    world.players.any { it.playerId == character.id }
