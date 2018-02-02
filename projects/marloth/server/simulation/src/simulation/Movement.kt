package simulation

import commanding.CommandType
import haft.Commands
import mythic.sculpting.FlexibleEdge
import mythic.spatial.*
import org.joml.minus
import org.joml.plus
import org.joml.xy

fun hitsWall(edge: FlexibleEdge, position: Vector3, radius: Float) =
    lineIntersectsCircle(edge.first.xy, edge.second.xy, position.xy, radius)

fun getEdgeNormal(first: Vector2, second: Vector2): Vector2 {
  val combination = first - second
  return Vector2(combination.y, -combination.x).normalize()
}

fun checkWallCollision(source: Vector3, originalOffset: Vector3, world: World): Vector3? {
  var offset = originalOffset + 0f
  val maxLength = offset.length()
  val newPosition = source + offset
  val radius = 0.8f
  val broadRadius = radius + maxLength
  val walls = world.meta.walls
      .filter { wall -> hitsWall(wall.edges[1], newPosition, broadRadius) && offset.dot(wall.normal) < 0f }
      .map {
        val edge = it.edges[1]
//        val dot2 = offset.dot(it.normal)C
//        println(dot2)
        val hitPoint = projectPointOntoLine(source.xy, edge.first.xy, edge.second.xy)
        val gap = hitPoint.distance(source.xy) - radius
        Triple(it, hitPoint, gap)
      }
      .sortedBy { it.first.normal.dot(offset) }

  if (walls.size > 0) {
    if (walls.size > 3)
      throw Error("Not supported.")

    val slideVectors = walls.map { (face, _, _) ->
      face.normal * face.normal.dot(offset)
    }

    val gapVectors = walls.map { (_, _, gap) ->
      ((offset + 0f).normalize() * gap)
    }
    if (walls.size == 2) {
//      val gapVector = gapVectors[0] + gapVectors[1]
//      offset = gapVector
      val firstEdge = walls[0].first.edges[1]
      val secondEdge = walls[1].first.edges[1]

      // Get the points of either edge ordered by the shared point and then the unshared point
      val rightEdge = if (firstEdge.first === secondEdge.second)
        secondEdge
      else
        firstEdge

      val knee = walls[0].first.normal + walls[1].first.normal
      val angle = getAngle(
          knee.xy.normalize(),
          (rightEdge.first - rightEdge.second).xy.normalize()
      )
      if (Math.abs(angle) <= Pi / 2) {
//        val dot2 = offset.dot(walls[0].first.normal + walls[0].first.normal)
//        println(dot2)
//        if (dot2 < 0f) {
        offset = gapVectors[0] + gapVectors[1]
//        println(offset)
//        }
      } else {
        offset = offset + gapVectors[0] - slideVectors[0]
      }
//      println("2 " + walls[0].first.normal + " | " + offset + " | " + gapVectors[0] + " | " + slideVectors[0])
    } else {
      offset = offset + gapVectors[0] - slideVectors[0]
//      println("1 " + walls[0].first.normal + " | " + offset + " | " + gapVectors[0] + " | " + slideVectors[0])
//      println("1 " + walls[0].first.normal + " | " + offset)
    }

    val offsetLength = offset.length()
    if (offsetLength > maxLength) {
      offset.normalize().mul(maxLength)
    }
  }
  return source + offset
}

fun movePlayer(world: World, player: Player, commands: Commands<CommandType>, delta: Float) {

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
