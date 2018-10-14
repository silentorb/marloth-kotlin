package physics

import simulation.CommandType
import mythic.sculpting.FlexibleEdge
import mythic.sculpting.FlexibleFace
import mythic.spatial.*
import simulation.*

fun hitsWall(edge: FlexibleEdge, position: Vector3, radius: Float) =
    lineIntersectsCircle(edge.first.xy(), edge.second.xy(), position.xy(), radius)

fun getEdgeNormal(first: Vector2, second: Vector2): Vector2 {
  val combination = first - second
  return Vector2(combination.y, -combination.x).normalize()
}

fun getCollisionWallsIncludingNeighbors(world: Realm, node: Node): Sequence<FlexibleFace> {
  return node.neighbors
      .plus(node)
      .flatMap { it.walls.asSequence() }
      .filter(isWall)
      .distinct()
}

fun wallsInCollisionRange(world: Realm, node: Node): List<FlexibleFace> {
  return node.walls
      .filter(isWall)
      .distinct()
}

data class WallCollision3(
    val wall: FlexibleFace,
    val hitPoint: Vector2,
    val directGap: Float,
    val travelingGap: Float
)

data class MovingBody(
    val radius: Float,
    val position: Vector3
)

fun getCollisionDetails(face: FlexibleFace, body: MovingBody, offset: Vector3): WallCollision3 {
  val edge = getFloor(face)
  val initialPoint = projectPointOntoLineSegment(body.position.xy(), edge.first.xy(), edge.second.xy())
      ?: listOf(edge.first.xy(), edge.second.xy()).sortedBy { it.distance(body.position.xy()) }.first()

  val directGap = initialPoint.distance(body.position.xy()) - body.radius

  val floorEdge = getFloor(face)
  val directPoint = body.position + (Vector3(initialPoint.x, initialPoint.y) - body.position).normalize() * body.radius
  val travelingPoint = getLineAndLineIntersection(directPoint, directPoint + offset, floorEdge.first, floorEdge.second)!!
  val travelingGap = directPoint.distance(Vector3(travelingPoint.x, travelingPoint.y, 0f))
  return WallCollision3(face, initialPoint, directGap, travelingGap)
}

fun getWallCollisions(body: MovingBody, offset: Vector3, walls: Collection<FlexibleFace>): List<WallCollision3> {
  val maxLength = offset.length()
  val broadRadius = body.radius + maxLength
  val hitWalls = walls
      .filter { wall -> hitsWall(getFloor(wall).edge, body.position, broadRadius) && offset.dot(wall.normal) < 0f }

  if (body.position.x + offset.x < 0.5f) {
    val k = 0
  }
  val result = hitWalls
      .map { getCollisionDetails(it, body, offset) }
//      .filter { it.directGap < offset.length() && it.travelingGap < offset.length() }
      .sortedBy { it.directGap }
      .toList()

  return if (result.size == 1 && result.first().travelingGap > offset.length())
    listOf()
  else
    result
}

data class WallCollision(
    val walls: List<FlexibleFace>,
    val offset: Vector3
)

fun slideCollides(collisions: List<Collision>, newPosition: Vector3, radius: Float): Boolean {
  return collisions.any {
    hitsWall(getFloor(it.wall!!).edge, newPosition, radius)
  }
}

private var lastTemp = 0
private var lastOffset = Vector3()
private var lastPosition = Vector3()

fun limitVector(max: Float, value: Vector3) =
    if (value.length() > max)
      value.normalize() * max
    else
      value

fun getWallCollisionMovementOffset(collisions: List<WallCollision3>, offset: Vector3, body: MovingBody): WallCollision {
  val walls = collisions.map { it.wall }
  val firstWall = walls.first()
  val normalizedOffset = offset.normalize()
  val floorEdge = getFloor(firstWall)
  val wallVector = Vector3(floorEdge.first - floorEdge.second).normalize()
  val slideVector = (wallVector * wallVector.dot(offset)).normalize()

//  val firstGap = collisions.first().gap
  val gapOffset = normalizedOffset * collisions.first().travelingGap
  val remainingLength = offset.length() - gapOffset.length()
  val slideOffset = slideVector * remainingLength
  val modifiedSlideOffset = if (collisions.size > 1) {
    val secondBody = MovingBody(
        radius = body.radius,
        position = body.position + gapOffset
    )
    val newCollisions = collisions.drop(1).map { getCollisionDetails(it.wall, secondBody, slideOffset) }
    val backup = newCollisions.map { it.travelingGap }.filter { it >= 0f }.sorted().firstOrNull()
    lastTemp = 1

    val result = if (backup != null) {
//      println(" " + slideOffset + " " + slideVector * backup)
      slideOffset.normalize() * backup
    } else {
//      println(newCollisions.first().gap)
      slideOffset
    }
    result
  } else {
    lastTemp = 2
    slideOffset
  }
  val finalOffset = gapOffset + modifiedSlideOffset
  if (body.position.x + finalOffset.x < 0.5f) {
    val k = 0
  }
  if (body.position.y + finalOffset.y > -0.5f) {
    val k = 0
  }
//  println(" " + firstWall.hashCode() + " " + body.position)
  lastOffset = finalOffset
  lastPosition = body.position
  return WallCollision(walls, finalOffset)
}

fun checkWallCollision(body: MovingBody, originalOffset: Vector3, walls: List<WallCollision3>): Vector3 {
  var offset = originalOffset + 0f
  val maxLength = offset.length()

  return if (walls.size > 0) {
    val collision = getWallCollisionMovementOffset(walls, offset, body)
    offset = collision.offset
    val offsetLength = offset.length()
    if (offsetLength > maxLength) {
      offset.normalize() * maxLength
    }
    body.position + offset
  } else {
    lastTemp = 0
    body.position + offset
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

//fun setCharacterFacing(child: Character, lookAt: Vector3) {
//  val angle = getLookAtAngle(lookAt)
//  child.facingRotation.z = angle
//}
