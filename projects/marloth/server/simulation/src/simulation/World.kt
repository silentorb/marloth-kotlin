package simulation

import commanding.CommandType
import haft.Commands
import mythic.sculpting.HalfEdgeFace
import org.joml.plus
import mythic.spatial.Vector3

typealias Players = List<Player>

data class MeshGroups(
    val floors: List<HalfEdgeFace>,
    val walls: List<HalfEdgeFace>
)

data class MetaWorld(
    val abstractWorld: AbstractWorld,
    val structureWorld: StructureWorld,
    val groups: MeshGroups
)

data class World(val meta: MetaWorld, val players: Players = listOf(Player(0))) {

}

fun applyPlayerCommands(player: Player, commands: Commands<CommandType>, delta: Float) {
  if (commands.isEmpty())
    return

  val offset = Vector3()
  val speed = 6f

  for (command in commands) {
    val rate = speed * command.value * delta
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

fun applyCommands(players: Players, commands: Commands<CommandType>, delta: Float) {
  for (player in players) {
    applyPlayerCommands(player, commands.filter({ it.target == player.id }), delta)
  }
}

fun updateWorld(world: World, commands: Commands<CommandType>, delta: Float) {
  applyCommands(world.players, commands, delta)
}