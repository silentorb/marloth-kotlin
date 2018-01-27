package simulation

import commanding.CommandType
import haft.Commands
import mythic.sculpting.FlexibleEdge
import mythic.sculpting.FlexibleFace
import mythic.spatial.*
import org.joml.plus
import org.joml.minus
import org.joml.xy

typealias Players = List<Player>

data class World(val meta: AbstractWorld, val players: Players = listOf(Player(0))) {

}

fun hitsWall(edge: FlexibleEdge, position: Vector3, radius: Float) =
    lineIntersectsCircle(edge.first.xy, edge.second.xy, position.xy, radius)

fun checkWallCollision(source: Vector3, originalOffset: Vector3, world: World): Vector3? {
  var offset = originalOffset + 0f
  val newPosition = source + offset
  val radius = 0.8f
  val walls = world.meta.walls
      .filter { wall -> hitsWall(wall.edges[1], newPosition, radius) }
      .map {
        val edge = it.edges[1]
        val hitPoint = projectPointOntoLine(source.xy, edge.first.xy, edge.second.xy)
        val gap = Math.max(hitPoint.distance(source.xy) - radius, 0f)
        Triple(edge, hitPoint, gap)
      }
      .sortedBy { it.third }

  for ((edge, hitPoint, gap) in walls) {
    val gapClose = ((offset + 0f).normalize() * gap)
    val edgeVector = (edge.second - edge.first).xy.normalize()
    val angle = -getAngle(offset.xy, edgeVector)
    val range = angle / (Pi * 0.5f) * (offset.length() - gap)
    val slideVector = edgeVector * range
    offset = gapClose + Vector3(slideVector, 0f)
    println("* " + gap.toString() + " | " + angle + " | " + range + " | " + angle / (Pi * 0.5f))
  }
//  println(offset.x.toString() + ", " + offset.y.toString() + " | " + originalOffset.length().toString() + " | " + offset.length().toString())
  return source + offset
}

class WorldUpdater(val world: World) {

  fun movePlayer(player: Player, commands: Commands<CommandType>, delta: Float) {

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

    if (offset != Vector3()) {
      val newPosition = checkWallCollision(player.position, offset, world)

      if (newPosition != null)
        player.position = newPosition
    }
  }

  fun applyPlayerCommands(player: Player, commands: Commands<CommandType>, delta: Float) {
    if (commands.isEmpty())
      return

    movePlayer(player, commands, delta)
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
