package simulation.physics.old

import mythic.ent.Id
import mythic.sculpting.ImmutableEdge
import mythic.sculpting.ImmutableFace
import mythic.spatial.*
import org.joml.Vector2fMinimal
import simulation.input.CommandType
import simulation.input.Commands
import simulation.misc.*

fun hitsWall(edge: ImmutableEdge, position: Vector3, radius: Float) =
    lineIntersectsCircle(edge.first.xy(), edge.second.xy(), position.xy(), radius)
/*
fun getEdgeNormal(first: Vector2, second: Vector2): Vector2 {
  val combination = first - second
  return Vector2(combination.y, -combination.x).normalize()
}

fun getCollisionWallsIncludingNeighbors(world: Realm, node: Node): Sequence<ImmutableFace> {
  return getPathNeighbors(node)
      .plus(node)
      .flatMap { it.walls.asSequence() }
      .filter(isSolidWall)
      .distinct()
}
*/

//fun wallsInCollisionRange(realm: Realm, node: Id): List<Id> {
//  return getPathNeighbors(realm.nodeTable, realm.faces, node).toList()
//      .plus(realm.nodeTable[node]!!)
//      .flatMap { n -> n.walls.filter { isSolidWall(realm.faces[it]!!) } }
//      .distinct()
//}

data class WallCollision3(
    val wall: ImmutableFace,
    val hitPoint: Vector2,
    val directGap: Float,
    val travelingGap: Float
)

data class MovingBody(
    val radius: Float,
    val position: Vector3
)

fun getCollisionDetails(face: ImmutableFace, body: MovingBody, offset: Vector3): WallCollision3 {
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

fun getWallCollisions(body: MovingBody, offset: Vector3, walls: Collection<ImmutableFace>): List<WallCollision3> {
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
    val walls: List<ImmutableFace>,
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

fun getSlideOffset(firstWall: ImmutableFace, others: List<WallCollision3>, offset: Vector3, body: MovingBody, remainingLength: Float): Vector3 {
  val floorEdge = getFloor(firstWall)
  val wallVector = (floorEdge.first - floorEdge.second).normalize()
  val wallDot = wallVector.dot(offset)
  val slideVector = (wallVector * wallDot).normalize()
  val slideStrength = Math.min(1f, Math.abs(wallDot) * 10f)
  val slideOffset = slideVector * remainingLength * slideStrength
  val newCollisions = others
      .filter { it.wall.normal.dot(slideVector) < 0f }
      .map { getCollisionDetails(it.wall, body, slideOffset) }

//  val newCollisions2 = newCollisions1
//      .filter { c ->
//        val floor = getFloor(c.wall)
//        floor.vertices.all { it.distance(Vector3(c.hitPoint.x, c.hitPoint.y, 0f)) > epsilon }
//      }
  val backup = newCollisions.map { it.travelingGap }.sorted().firstOrNull()
  lastTemp = 1

  val result = if (backup != null) {
    slideOffset.normalize() * Math.min(remainingLength, backup)
  } else {
    slideOffset
  }
  return result
}

var _i = 0
fun getWallCollisionMovementOffset(collisions: List<WallCollision3>, offset: Vector3, body: MovingBody): WallCollision {
//  assert(collisions.size < 3)
  val walls = collisions.map { it.wall }
  val firstWall = walls.first()
  val normalizedOffset = offset.normalize()
  val initialTravelingGap = collisions.first().travelingGap
  val travelingGap = if (initialTravelingGap < 0f)
    Math.max(-offset.length(), initialTravelingGap)
  else
    initialTravelingGap

  val gapOffset = normalizedOffset * travelingGap
  val remainingLength = offset.length() - gapOffset.length()
//  val slideOffset = slideVector * remainingLength * slideStrength
  val slideOffset = if (collisions.size > 1) {
    val secondBody = MovingBody(
        radius = body.radius,
        position = body.position + gapOffset
    )
    val slideOffsets = collisions.mapIndexed { index, collision ->
      getSlideOffset(collision.wall, collisions.filterIndexed { i, _ -> i != index }, offset, secondBody, remainingLength)
    }
    slideOffsets.first()
//    val sortedSlideOffsets = slideOffsets.sortedByDescending { it.length() }
//    println("" + _i++ + " " + slideOffsets.indexOf(sortedSlideOffsets.first()))
//    sortedSlideOffsets.first()
  } else {
    lastTemp = 2
    getSlideOffset(firstWall, listOf(), offset, body, remainingLength)
  }
  val combinedOffset = gapOffset + slideOffset
  if (offset.dot(combinedOffset) <= 0f) {
    val k = 0
  }
  if (combinedOffset.dot(lastOffset) < 0.9f) {
    val k = 0
  }
  val finalOffset = if (combinedOffset.length() > offset.length()) {
    combinedOffset.normalize() * offset.length() * (1 - 0.0000001f)
  } else
    combinedOffset

//  if (finalOffset.length() > offset.length()) {
//    assert(false)
//  }
//
//  val doubleCheck = getCollisionDetails(firstWall, MovingBody(
//      radius = body.radius,
//      position = body.position + finalOffset
//  ), offset)
//  if (doubleCheck.travelingGap < 0f) {
//    val da = getCollisionDetails(firstWall, MovingBody(
//        radius = body.radius,
//        position = body.position + gapOffset
//    ), offset)
//    val db = getCollisionDetails(firstWall, MovingBody(
//        radius = body.radius,
//        position = body.position + slideOffset
//    ), offset)
//    throw Error("Overshot wall")
//  }

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
  return if (offset == Vector3.zero)
    Vector3.zero
  else {
    if (offset.length() > 1f)
      offset.normalize()
    else
      offset
  }
}

fun getLookAtAngle(lookAt: Vector2fMinimal) =
    getAngle(Vector2(1f, 0f), lookAt.xy())
