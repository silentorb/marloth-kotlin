package simulation

import commanding.CommandType
import commanding.Commands
import org.joml.Vector3f
import org.joml.minus
import org.joml.plus
import spatial.Vector3

typealias Players = Array<Player>

class World {
  val players: Players = arrayOf(Player(0))
}

fun applyPlayerCommands(player: Player, commands: Commands) {
  if (commands.isEmpty())
    return

  val offset = Vector3()
  val speed = 1

  for (command in commands) {
    val rate = speed * command.value
    when (command.type) {
      CommandType.moveLeft -> offset.x -= rate
      CommandType.moveRight -> offset.x += rate
      CommandType.moveUp -> offset.y += rate
      CommandType.moveDown -> offset.y -= rate
    }
  }

  if (offset != Vector3())
    player.position += offset
}

fun applyCommands(players: Players, commands: Commands) {
  for (player in players) {
    applyPlayerCommands(player, commands.filter({ it.target == player.id }).toTypedArray())
  }
}

fun updateWorld(world: World, commands: Commands) {
  applyCommands(world.players, commands)
}