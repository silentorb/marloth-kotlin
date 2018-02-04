package simulation

import commanding.CommandType
import haft.Commands
import mythic.sculpting.FlexibleEdge
import mythic.sculpting.FlexibleFace
import mythic.spatial.*
import org.joml.plus
import org.joml.minus
import org.joml.xy
import kotlin.math.max

val maxPlayerCount = 4

typealias Players = List<Player>

data class World(
    val meta: AbstractWorld,
    val players: MutableList<Player> = mutableListOf(Player(1))
)

class WorldUpdater(val world: World) {

  fun applyPlayerCommands(player: Player, commands: Commands<CommandType>, delta: Float) {
    if (commands.isEmpty())
      return

    movePlayer(world, player, commands, delta)
  }

  fun applyCommands(players: Players, commands: Commands<CommandType>, delta: Float) {
    for (player in players) {
      applyPlayerCommands(player, commands.filter({ it.target == player.id }), delta)
    }

    val remainingCommands = commands.filter({ it.target == 0 || it.target > maxPlayerCount })
    for (command in remainingCommands) {
      if (command.type == CommandType.joinGame) {
        world.players.add(createPlayer(world.meta, world.players.size + 1))
      }
    }
  }

  fun update(commands: Commands<CommandType>, delta: Float) {
    applyCommands(world.players, commands, delta)
  }
}

fun createPlayer(abstractWorld: AbstractWorld, id: Int) =
    Player(id, abstractWorld.nodes.first().position + Vector3(0f, 0f, 1f))