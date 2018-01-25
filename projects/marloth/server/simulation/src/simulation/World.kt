package simulation

import commanding.CommandType
import haft.Commands
import mythic.sculpting.FlexibleEdge
import mythic.sculpting.FlexibleFace
import org.joml.plus
import mythic.spatial.Vector3
import mythic.spatial.lineIntersectsCircle
import mythic.spatial.times
import org.joml.xy

typealias Players = List<Player>

//data class MeshGroups(
//    val floors: List<FlexibleFace>,
//    val walls: List<FlexibleFace>
//)

//data class MetaWorld(
//    val abstractWorld: AbstractWorld,
//    val groups: MeshGroups
//)

data class World(val meta: AbstractWorld, val players: Players = listOf(Player(0))) {

}

fun hitsWall(edge: FlexibleEdge, position: Vector3, radius: Float) =
    lineIntersectsCircle(edge.first.xy, edge.second.xy, position.xy, radius)

class WorldUpdater(val world: World) {

  fun applyPlayerCommands(player: Player, commands: Commands<CommandType>, delta: Float) {
    if (commands.isEmpty())
      return

    var offset = Vector3()
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

    if (offset != Vector3()) {
      val newPosition = player.position + offset
      if (world.meta.walls.any { wall -> hitsWall(wall.edges[1], newPosition, 0.8f) }) {
        offset =  Vector3()
      }

      if (offset != Vector3())
        player.position += offset
    }
  }

  fun applyCommands(players: Players, commands: Commands<CommandType>, delta: Float) {
    for (player in players) {
      applyPlayerCommands(player, commands.filter({ it.target == player.id }), delta)
    }
  }

  fun update(commands: Commands<CommandType>, delta: Float) {
    applyCommands(world.players, commands, delta)
  }
}
