package physics

import simulation.getPathNeighbors
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
  return getPathNeighbors(node)
      .plus(node)
      .flatMap { it.walls.asSequence() }
      .filter(isSolidWall)
      .distinct()
}

fun wallsInCollisionRange(world: Realm, node: Node): List<FlexibleFace> {
  return getPathNeighbors(node).toList()
      .plus(node)
      .flatMap { it.walls.filter(isSolidWall) }
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
  val travelingPoint = getLineAndLineIntersection(directPoint, directPoint + offset, floorEdge.first, floorEdge.second)
  val travelingGap = if (travelingPoint != null)
    directPoint.distance(Vector3(travelingPoint.x, travelingPoint.y, 0f))
  else
    directGap

  if (directGap < 0f) {
    val k = 0
  }
  val directedTravelingGap = if (directGap < 0f)
    -travelingGap
  else
    travelingGap
  return WallCollision3(face, initialPoint, directGap, directedTravelingGap)
}

fun getWallCollisions(body: MovingBody, offset: Vector3, walls: Collection<FlexibleFace>): List<WallCollision3> {
  val maxLength = offset.length()
  val broadRadius = body.radius + maxLength
  val hitWalls = walls
      .filter { wall -> hitsWall(getFloor(wall).edge, body.position, broadRadius) && offset.dot(wall.normal) < 0f }

  if (hitWalls.any()) {
    val k = 0
  } else {
    val k = 1
  }
  val result = hitWalls
      .map { getCollisionDetails(it, body, offset) }
//      .filter { it.directGap < offset.length() && it.travelingGap < offset.length() }
      .sortedBy { it.travelingGap }
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

private var lastTemp = 0
private var lastOffset = Vector3()
private var lastPosition = Vector3()

fun limitVector(max: Float, value: Vector3) =
    if (value.length() > max)
      value.normalize() * max
    else
      value

fun getSlideOffset(firstWall: FlexibleFace, others: List<WallCollision3>, offset: Vector3, body: MovingBody, remainingLength: Float): Vector3 {
  val floorEdge = getFloor(firstWall)
  val wallVector = Vector3(floorEdge.first - floorEdge.second).normalize()
  val wallDot = wallVector.dot(offset)
  val slideVector = (wallVector * wallDot).normalize()
  val slideStrength = Math.min(1f, Math.abs(wallDot) * 10f)
  val slideOffset = slideVector * remainingLength * slideStrength
  val newCollisions = others
      .filter { it.wall.normal.dot(slideVector) <= 0f }
      .map { getCollisionDetails(it.wall, body, slideOffset) }
  val backup = newCollisions.map { it.travelingGap }.sorted().firstOrNull()
  lastTemp = 1

  val result = if (backup != null) {
//      println(" " + slideOffset + " " + slideVector * backup)
    slideOffset.normalize() * Math.min(remainingLength, backup)
  } else {
//      println(newCollisions.first().gap)
    slideOffset
  }
  return result
}

fun getWallCollisionMovementOffset(collisions: List<WallCollision3>, offset: Vector3, body: MovingBody): WallCollision {
  val walls = collisions.map { it.wall }
  val firstWall = walls.first()
  val normalizedOffset = offset.normalize()
//  val floorEdge = getFloor(firstWall)
//  val wallVector = Vector3(floorEdge.first - floorEdge.second).normalize()
//  val wallDot = wallVector.dot(offset)
//  val slideVector = (wallVector * wallDot).normalize()

//  val slideStrength = Math.min(1f, Math.abs(wallDot) * 10f)
  val initialTravelingGap = collisions.first().travelingGap
  val travelingGap = if (initialTravelingGap < 0f)
    Math.max(-offset.length(), initialTravelingGap)
  else
    initialTravelingGap

  val gapOffset = normalizedOffset * travelingGap
  val remainingLength = offset.length() - gapOffset.length()
//  val slideOffset = slideVector * remainingLength * slideStrength
  val modifiedSlideOffset = if (collisions.size > 1) {
    val secondBody = MovingBody(
        radius = body.radius,
        position = body.position + gapOffset
    )
    val slideOffsets = collisions.mapIndexed { index, collision ->
      getSlideOffset(collision.wall, collisions.filterIndexed { i, _ -> i != index }, offset, secondBody, remainingLength)
    }
    slideOffsets.sortedByDescending { it.length() }.first()

//    val newCollisions = collisions.drop(1).map { getCollisionDetails(it.wall, secondBody, slideOffset) }
//    val backup = newCollisions.map { it.travelingGap }.sorted().firstOrNull()
//    lastTemp = 1
//
//    val result = if (backup != null) {
////      println(" " + slideOffset + " " + slideVector * backup)
//      slideOffset.normalize() * backup
//    } else {
////      println(newCollisions.first().gap)
//      slideOffset
//    }
//    result
  } else {
    lastTemp = 2
    getSlideOffset(firstWall, listOf(), offset, body, remainingLength)
//    slideOffset
  }
  val finalOffset = gapOffset + modifiedSlideOffset
  if (finalOffset.x > 0.001f) {
    val k = 0
  }
  if (finalOffset.length() > offset.length()) {
    val k = 0
  }

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
