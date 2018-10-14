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

fun getCollisionWallsIncludingNeighbors(world: Realm, node: Node): Sequence<FlexibleFace> {
  return node.neighbors
      .plus(node)
      .flatMap { it.walls.asSequence() }
      .filter(isWall)
      .distinct()
}

fun getCollisionWalls(world: Realm, node: Node): List<FlexibleFace> {
  return node.walls
      .filter(isWall)
      .distinct()
}

data class WallCollision3(
    val wall: FlexibleFace,
    val hitPoint: Vector2,
    val gap: Float
)

private const val characterRadius = 0.5f

fun getCollisionDetails(face: FlexibleFace, source: Vector3): WallCollision3 {
  val edge = getFloor(face)
  val hitPoint = projectPointOntoLineSegment(source.xy(), edge.first.xy(), edge.second.xy())
  return if (hitPoint != null) {
    val gap = hitPoint.distance(source.xy()) - characterRadius
    WallCollision3(face, hitPoint, gap)
  } else {
    val nearestEnd = listOf(edge.first.xy(), edge.second.xy()).sortedBy { it.distance(source.xy()) }.first()
    val gap = nearestEnd.distance(source.xy()) - characterRadius
    WallCollision3(face, nearestEnd, gap)
  }
}

fun findCollisionWalls(source: Vector3, originalOffset: Vector3, world: Realm, node: Node): List<WallCollision3> {
  val offset = originalOffset + 0f
  val maxLength = offset.length()
  val broadRadius = characterRadius + maxLength
  val walls = getCollisionWalls(world, node)
//  val walls = world.walls
//      .filter(isWall)
  return walls
      .filter(isWall)
      .filter { wall -> hitsWall(getFloor(wall).edge, source, broadRadius) && offset.dot(wall.normal) < 0f }
      .map { getCollisionDetails(it, source) }
      .sortedBy { it.gap }
      .toList()
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

fun getWallCollisionMovementOffset(collisions: List<Collision>, offset: Vector3, position: Vector3): WallCollision {
  assert(collisions.none { it.wall == null })
  val walls = collisions.mapNotNull { it.wall }
  val firstWall = walls.first()
  val normalizedOffset = offset.normalize()
  val slideVector = -(firstWall.normal * firstWall.normal.dot(offset)).normalize()

  val firstGap = collisions.first().gap
  val gapOffset = limitVector(offset.length(), normalizedOffset * firstGap)
  val remainingLength = offset.length() - gapOffset.length()
  val slideOffset = slideVector * remainingLength
  val modifiedSlideOffset = if (walls.size > 1) {
    val secondPosition = position + gapOffset + slideOffset
    val newCollisions = collisions.drop(1).map { getCollisionDetails(it.wall!!, secondPosition) }
    val backup = newCollisions.map { it.gap }.filter { it < 0f }.sorted().firstOrNull()
    lastTemp = 1
    val edges = walls.map { getFloor(it).edge }
    val ideal2D = getLineAndLineIntersection(
        Vector3(edges[0].first) + walls[0].normal * characterRadius,
        Vector3(edges[0].second) + walls[0].normal * characterRadius,
        Vector3(edges[1].first) + walls[1].normal * characterRadius,
        Vector3(edges[1].second) + walls[1].normal * characterRadius
    )!!
    val ideal = Vector3(ideal2D.x, ideal2D.y, 0f)

    val result = if (backup != null) {
      println(" " + slideOffset + " " + slideVector * backup)
      slideOffset + slideVector * backup
    } else {
//      println(newCollisions.first().gap)
      slideOffset
    }
    if (position.distance(ideal) <= offset.length()) {
      if (ideal.roughlyEquals(position + result)) {
        println("a")
      } else {
        println("b")
      }
    }
    result
  } else {
    lastTemp = 2
    slideOffset
  }
  val finalOffset = gapOffset + modifiedSlideOffset
  println(" " + firstWall.hashCode() + " " + position)
  lastOffset = finalOffset
  lastPosition = position
  return WallCollision(walls, finalOffset)
}

fun checkWallCollision(source: Vector3, originalOffset: Vector3, walls: List<Collision>): Vector3 {
  var offset = originalOffset + 0f
  val maxLength = offset.length()

  return if (walls.size > 0) {
    val collision = getWallCollisionMovementOffset(walls, offset, source)
    offset = collision.offset
    val offsetLength = offset.length()
    if (offsetLength > maxLength) {
      offset.normalize() * maxLength
    }
    source + offset
  } else {
    lastTemp = 0
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

//fun setCharacterFacing(child: Character, lookAt: Vector3) {
//  val angle = getLookAtAngle(lookAt)
//  child.facingRotation.z = angle
//}
