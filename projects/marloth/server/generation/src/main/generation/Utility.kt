package generation

fun getNodeDistance(first: Node, second: Node): Float =
    first.position.distance(second.position) - first.radius - second.radius

fun overlaps2D(first: Node, second: Node): Boolean {
  val distance = getNodeDistance(first, second)
  return distance < 0
}
