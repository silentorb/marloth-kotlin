package generation.misc

import generation.abstracted.tunnelRadius
import mythic.spatial.*
import simulation.misc.Node

fun <T> divide(sequence: Sequence<T>, filter: (T) -> Boolean) =
    Pair(sequence.filter(filter), sequence.filter { !filter(it) })

fun <T> divide(sequence: List<T>, filter: (T) -> Boolean) =
    Pair(sequence.filter(filter), sequence.filter { !filter(it) })

fun getNodeDistance(first: Node, second: Node): Float =
    first.position.distance(second.position) - first.radius - second.radius

fun getNodeDistance(first: Node, position: Vector3, radius: Float): Float =
    first.position.distance(position) - first.radius - radius

fun overlaps2D(first: Node, second: Node): Boolean {
  val distance = getNodeDistance(first, second)
  return distance < 0
}

fun forkVector(point: Vector2, direction: Vector2, length: Float): List<Vector2> {
  val perpendicular = Vector2(direction.y, -direction.x) * length
  return listOf(point + perpendicular, point - perpendicular)
}

fun circleIntersection(aPoint: Vector2, aRadius: Float, bPoint: Vector2, bRadius: Float): List<Vector2> {
  val distance = aPoint.distance(bPoint)
  val aLength = (aRadius * aRadius - bRadius * bRadius + distance * distance) / (distance * 2)
  val direction = (bPoint - aPoint).normalize()
  val center = aPoint + direction.normalize() * aLength
  val pLength = Math.sqrt((aRadius * aRadius - aLength * aLength).toDouble()).toFloat()
  return forkVector(center, direction, pLength)
}

fun getAngle(first: Vector2, second: Vector2): Float {
  val third = second - first
  return Math.atan2(third.y.toDouble(), third.x.toDouble()).toFloat()
}

fun getAngle(second: Vector2): Float {
  return getAngle(Vector2(1f, 0f), second)
}

fun getAngle(node: Node, second: Vector3) = getAngle(node.position.xy(), second.xy())

fun project2D(angle: Float, distance: Float): Vector2 {
  return Vector2(
      Math.cos(angle.toDouble()).toFloat() * distance,
      Math.sin(angle.toDouble()).toFloat() * distance
  )
}

fun <T> toPairs(corners: Sequence<T>): List<Pair<T, T>> {
  val result: MutableList<Pair<T, T>> = mutableListOf()
  var previous = corners.first()
  for (next in corners.drop(1)) {
    result.add(Pair(previous, next))
    previous = next
  }
  return result
}

fun isInsideCircle(point: Vector2, center: Vector2, radius: Float): Boolean {
  val a = point.x - center.x
  val b = point.y - center.y
  return a * a + b * b < radius * radius
}

fun isInsideNode(point: Vector2, node: Node) = isInsideCircle(point, node.position.xy(), node.radius)

fun getCenter(first: Node, second: Node): Vector3 {
  val distance = first.position.distance(second.position)
  val mod = (distance - first.radius - second.radius) / 2 + first.radius
  return first.position + (second.position - first.position) / distance * mod
}

fun roughlyEquals(first: Vector2, second: Vector2, range: Float): Boolean {
  val r = range / 2
  return first.x > second.x - r && first.x < second.x + r
      && first.y > second.y - r && first.y < second.y + r
}

tailrec fun <T> crossMap(items: Collection<T>, accumulator: List<T> = listOf(), filter: (T, T) -> Boolean): List<T> {
  val sequence = items
      .drop(1)

  if (sequence.firstOrNull() == null)
    return accumulator

  val additions = sequence.filter { filter(items.first(), it) }
  val accumulation = accumulator.plus(additions)
  return crossMap(sequence, accumulation, filter)
}

fun connectionOverlapsNeighborNodes(neighbors: List<Node>, first: Node, second: Node): Boolean {
  if (neighbors.any()) {
    val connectionCenter = getCenter(first, second)
    return neighbors.any { neighbor ->
      lineIntersectsCircle(first.position.xy(), second.position.xy(), neighbor.position.xy(), neighbor.radius + tunnelRadius)
//      neighbor.position.distance(connectionCenter) - neighbor.radius - tunnelRadius < 0f
    }
  }
  return false
}
