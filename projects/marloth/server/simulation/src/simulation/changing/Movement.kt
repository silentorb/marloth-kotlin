package simulation.changing

import simulation.CommandType
import mythic.sculpting.FlexibleEdge
import mythic.sculpting.FlexibleFace
import mythic.spatial.*
import physics.Collision
import simulation.*

fun hitsWall(edge: FlexibleEdge, position: Vector3, radius: Float) =
    lineIntersectsCircle(edge.first.xy(), edge.second.xy(), position.xy(), radius)

fun getEdgeNormal(first: Vector2, second: Vector2): Vector2 {
  val combination = first - second
  return Vector2(combination.y, -combination.x).normalize()
}

fun getCollisionWalls(world: Realm, node: Node): Sequence<FlexibleFace> {
  return node.neighbors
      .plus(node)
      .flatMap { it.walls.asSequence() }
      .filter(isWall)
      .distinct()
}

data class WallCollision3(
    val wall: FlexibleFace,
    val hitPoint: Vector2,
    val gap: Float
)


fun findCollisionWalls(source: Vector3, originalOffset: Vector3, world: Realm, node: Node): List<WallCollision3> {
  val offset = originalOffset + 0f
  val maxLength = offset.length()
  val newPosition = source + offset
  val radius = 0.5f
  val broadRadius = radius + maxLength
  val walls = getCollisionWalls(world, node)
//  val walls = world.walls
//      .filter(isWall)
  return walls
      .filter(isWall)
      .filter { wall -> hitsWall(wall.edges[0].edge, newPosition, broadRadius) && offset.dot(wall.normal) < 0f }
      .map { face ->
        val edge = face.edges[0]
        val hitPoint = projectPointOntoLineSegment(source.xy(), edge.first.xy(), edge.second.xy())
        if (hitPoint != null) {
          val gap = hitPoint.distance(source.xy()) - radius
          WallCollision3(face, hitPoint, gap)
        } else {
          val nearestEnd = listOf(edge.first.xy(), edge.second.xy()).sortedBy { it.distance(source.xy()) }.first()
          val gap = nearestEnd.distance(source.xy()) - radius
          WallCollision3(face, nearestEnd, gap)
        }
      }
      .sortedBy { it.wall.normal.dot(offset) }
      .toList()
}

data class WallCollision(
    val walls: List<FlexibleFace>,
    val offset: Vector3
)

fun getWallCollisionMovementOffset(collisions: List<Collision>, offset: Vector3): WallCollision {
  val walls = collisions.mapNotNull { it.wall }
  if (walls.size > 3) {
//    throw Error("Not supported.")
    // TODO Uncomment above line
  }
  val slideVectors = walls.map { face ->
    face.normal * face.normal.dot(offset)
  }

  val gapVectors = collisions.map { (_, _, _, _, gap) ->
    ((offset + 0f).normalize() * gap)
  }
  if (walls.size == 2) {
//      val gapVector = gapVectors[0] + gapVectors[1]
//      offset = gapVector
    val firstEdge = walls[0].edges[0]
    val secondEdge = walls[1].edges[0]

    // Get the points of either edge ordered by the shared point and then the unshared point
    val rightEdge = if (firstEdge.first === secondEdge.second)
      secondEdge
    else
      firstEdge

    val knee = walls[0].normal + walls[1].normal
    val angle = getAngle(
        (rightEdge.first - rightEdge.second).xy().normalize(),
        knee.xy().normalize()
    )
//    println("" + angle + " | " + knee.xy() + " | " + slideVectors[0].dot(walls[1].first.normal) + " | " + slideVectors[1].dot(walls[0].first.normal))
//    if (Math.abs(angle) <= Pi) {
    if (Math.abs(slideVectors[1].dot(walls[0].normal)) > 0.05f) {
      val dot2 = offset.dot(walls[0].normal + walls[0].normal)
//      println(dot2)
//        if (dot2 < 0f) {
      return WallCollision(walls, gapVectors[0] + gapVectors[1])
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
  return WallCollision(walls, offset + gapVectors[0] - slideVectors[0])
}

fun checkWallCollision(source: Vector3, originalOffset: Vector3, walls: List<Collision>): Vector3 {
  var offset = originalOffset + 0f
  val maxLength = offset.length()

  return if (walls.size > 0) {
    val collision = getWallCollisionMovementOffset(walls, offset)
    offset = collision.offset
    val offsetLength = offset.length()
    if (offsetLength > maxLength) {
      offset.normalize() * maxLength
    }
    source + offset
  } else {
    source + offset
  }
}

val playerMoveMap = mapOf(
    CommandType.moveLeft to Vector3(-1f, 0f, 0f),
    CommandType.moveRight to Vector3(1f, 0f, 0f),
    CommandType.moveUp to Vector3(0f, 1f, 0f),
    CommandType.moveDown to Vector3(0f, -1f, 0f)
)

val playerAttackMap = mapOf(
    CommandType.lookLeft to Vector3(-1f, 0f, 0f),
    CommandType.lookRight to Vector3(1f, 0f, 0f),
    CommandType.lookUp to Vector3(0f, 1f, 0f),
    CommandType.lookDown to Vector3(0f, -1f, 0f)
)

fun joinInputVector(commands: Commands, commandMap: Map<CommandType, Vector3>): Vector3? {
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
  return if (offset == Vector3())
    Vector3()
  else {
    offset.normalize()
    assert(!offset.x.isNaN() && !offset.y.isNaN())
    return offset
  }
}

fun getLookAtAngle(lookAt: Vector3) =
    getAngle(Vector2(1f, 0f), lookAt.xy())

//fun setCharacterFacing(character: Character, lookAt: Vector3) {
//  val angle = getLookAtAngle(lookAt)
//  character.facingRotation.z = angle
//}
