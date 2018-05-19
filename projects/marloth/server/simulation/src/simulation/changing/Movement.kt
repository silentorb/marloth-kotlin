package simulation.changing

import commanding.CommandType
import haft.Commands
import mythic.sculpting.FlexibleEdge
import mythic.sculpting.FlexibleFace
import mythic.spatial.*
import org.joml.minus
import org.joml.plus
import org.joml.times
import org.joml.xy
import simulation.Character
import simulation.Player
import simulation.ViewMode
import simulation.World

fun hitsWall(edge: FlexibleEdge, position: Vector3, radius: Float) =
    lineIntersectsCircle(edge.first.xy, edge.second.xy, position.xy, radius)

fun getEdgeNormal(first: Vector2, second: Vector2): Vector2 {
  val combination = first - second
  return Vector2(combination.y, -combination.x).normalize()
}

fun findCollisionWalls(source: Vector3, originalOffset: Vector3, world: World): List<Triple<FlexibleFace, Vector2, Float>> {
  val offset = originalOffset + 0f
  val maxLength = offset.length()
  val newPosition = source + offset
  val radius = 0.8f
  val broadRadius = radius + maxLength
  return world.meta.walls
      .filter { wall -> hitsWall(wall.edges[0], newPosition, broadRadius) && offset.dot(wall.normal) < 0f }
      .map {
        val edge = it.edges[0]
        val hitPoint = projectPointOntoLine(source.xy, edge.first.xy, edge.second.xy)
        if (hitPoint != null) {
          val gap = hitPoint.distance(source.xy) - radius
          Triple(it, hitPoint, gap)
        }
        else {
          val nearestEnd = listOf(edge.first.xy, edge.second.xy).sortedBy { it.distance(source.xy) }.first()
          val gap = nearestEnd.distance(source.xy) - radius
          Triple(it, nearestEnd, gap)
        }
      }
      .sortedBy { it.first.normal.dot(offset) }
}

fun getWallCollisionMovementOffset(walls: List<Triple<FlexibleFace, Vector2, Float>>, offset: Vector3): Vector3 {
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
    val firstEdge = walls[0].first.edges[0]
    val secondEdge = walls[1].first.edges[0]

    // Get the points of either edge ordered by the shared point and then the unshared point
    val rightEdge = if (firstEdge.first === secondEdge.second)
      secondEdge
    else
      firstEdge

    val knee = walls[0].first.normal + walls[1].first.normal
    val angle = getAngle(
        (rightEdge.first - rightEdge.second).xy.normalize(),
        knee.xy.normalize()
    )
//    println("" + angle + " | " + knee.xy + " | " + slideVectors[0].dot(walls[1].first.normal) + " | " + slideVectors[1].dot(walls[0].first.normal))
//    if (Math.abs(angle) <= Pi) {
      if (Math.abs(slideVectors[1].dot(walls[0].first.normal)) > 0.05f) {
        val dot2 = offset.dot(walls[0].first.normal + walls[0].first.normal)
        println(dot2)
//        if (dot2 < 0f) {
      return gapVectors[0] + gapVectors[1]
//        println(offset)
//        }
    }
  }
//    else {
//      return offset + gapVectors[0] - slideVectors[0]
//    }
////      println("2 " + walls[0].first.normal + " | " + offset + " | " + gapVectors[0] + " | " + slideVectors[0])
//  } else {
////      println("1 " + walls[0].first.normal + " | " + offset + " | " + gapVectors[0] + " | " + slideVectors[0])
////      println("1 " + walls[0].first.normal + " | " + offset)
//  }
  return offset + gapVectors[0] - slideVectors[0]
}

fun checkWallCollision(source: Vector3, originalOffset: Vector3, world: World): Vector3? {
  var offset = originalOffset + 0f
  val maxLength = offset.length()
  val walls = findCollisionWalls(source, originalOffset, world)
//        world.meta.walls
//        .filter { wall -> hitsWall(wall.edges[0], newPosition, broadRadius) && offset.dot(wall.normal) < 0f }
//        .map {
//          val edge = it.edges[0]
////        val dot2 = offset.dot(it.normal)C
////        println(dot2)
//          val hitPoint = projectPointOntoLine(source.xy, edge.first.xy, edge.second.xy)
//          val gap = hitPoint.distance(source.xy) - radius
//          Triple(it, hitPoint, gap)
//        }
//        .sortedBy { it.first.normal.dot(offset) }

  if (walls.size > 0) {
    offset = getWallCollisionMovementOffset(walls, offset)
    val offsetLength = offset.length()
    if (offsetLength > maxLength) {
      offset.normalize().mul(maxLength)
    }
  }
  return source + offset
}

val playerMoveMap = mapOf(
    CommandType.moveLeft to Vector3(-1f, 0f, 0f),
    CommandType.moveRight to Vector3(1f, 0f, 0f),
    CommandType.moveUp to Vector3(0f, 1f, 0f),
    CommandType.moveDown to Vector3(0f, -1f, 0f)
)

val playerLookMap = mapOf(
    CommandType.lookLeft to Vector3(0f, 0f, 1f),
    CommandType.lookRight to Vector3(0f, 0f, -1f),
    CommandType.lookUp to Vector3(0f, -1f, 0f),
    CommandType.lookDown to Vector3(0f, 1f, 0f)
)

val playerAttackMap = mapOf(
    CommandType.lookLeft to Vector3(-1f, 0f, 0f),
    CommandType.lookRight to Vector3(1f, 0f, 0f),
    CommandType.lookUp to Vector3(0f, 1f, 0f),
    CommandType.lookDown to Vector3(0f, -1f, 0f)
)

fun joinInputVector(commands: Commands<CommandType>, commandMap: Map<CommandType, Vector3>): Vector3? {
  val forces = commands.mapNotNull {
    val vector = commandMap[it.type]
    if (vector != null && it.value > 0)
      vector * it.value
    else
      null
  }
  if (forces.isEmpty())
    return null

  val offset = forces.reduce { a, b -> a + b }
  offset.normalize()
  assert(!offset.x.isNaN() && !offset.y.isNaN())
  return offset
}

fun setCharacterFacing(character: Character, lookAt: Vector3) {
  val angle = getAngle(Vector2(1f, 0f), lookAt.xy)
  character.facingRotation.z = angle
}

fun playerMove(world: World, player: Player, commands: Commands<CommandType>, delta: Float) {
  val body = player.character.body
  val speed = 6f
  var offset = joinInputVector(commands, playerMoveMap)

  if (offset != null) {
    if (player.viewMode == ViewMode.firstPerson)
      offset = (Quaternion().rotateZ(player.character.facingRotation.z - Pi / 2) * offset)!!
    else if (player.viewMode == ViewMode.topDown) {
      setCharacterFacing(player.character, offset)
    }
    val newPosition = checkWallCollision(body.position, offset * speed * delta, world)
    if (newPosition != null) {
      assert(!newPosition.x.isNaN() && !newPosition.y.isNaN())
      body.position = newPosition
//      println("" + body.position.x + ", " + body.position.y + "," + body.position.z)
    }
  }
}

fun playerRotate(player: Player, commands: Commands<CommandType>, delta: Float) {
  val character = player.character
  val speed = Vector3(1f, 0.01f, 0.03f)
  val offset = joinInputVector(commands, playerLookMap)

  if (offset != null) {
    character.facingRotation += offset * speed
  }
}