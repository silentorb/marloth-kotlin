package generation.abstracted.old

import generation.abstracted.maxInitialNodeSize
import generation.abstracted.minInitialNodeDistance
import generation.abstracted.minInitialNodeSize
import generation.misc.getNodeDistance
import mythic.ent.firstSortedBy
import mythic.spatial.Vector3
import mythic.spatial.withinRangeFast
import org.joml.Vector3i
import randomly.Dice
import simulation.misc.Node

val worldCellLength = 20
val maxTunnelLength = worldCellLength.toFloat() * 1.5f

private fun clipFloat(unitLength: Int, value: Float): Int =
    value.toInt() / unitLength

fun clipDimensions(cellLength: Int, dimensions: Vector3): Vector3i =
    Vector3i(
        clipFloat(cellLength, dimensions.x),
        clipFloat(cellLength, dimensions.y),
        clipFloat(cellLength, dimensions.z)
    )

private fun getPosition(dimensions: Vector3i, i: Int): Vector3 {
  val sliceSize = dimensions.x * dimensions.y
  val z = i / sliceSize
  val zRemainder = i - sliceSize * z
  val y = zRemainder / dimensions.x
  val x = zRemainder - y * dimensions.x
  return Vector3(
      x.toFloat() - dimensions.x * 0.5f,
      y.toFloat() - dimensions.y * 0.5f,
      z.toFloat() - dimensions.z * 0.5f
  )
}

fun calculateRadius(position: Vector3, nodes: List<Node>, dice: Dice): Float {
  val defaultMax = maxInitialNodeSize + minInitialNodeDistance

  val nearest = if (nodes.any()) {
    val n = nodes.map { it.position.distance(position) - it.radius }
    n
        .firstSortedBy { it }
  } else
    defaultMax

  val min = minInitialNodeSize
  val max = Math.min(nearest, defaultMax)
  assert(max > min)
  return dice.getFloat(min, max)
}

private fun getBoundaryNode(nodes: List<Node>, node: Node, axis: Int): Node? {
  val origin = node.position[axis]
  val pool = nodes
      .asSequence()
      .filter { it.id != node.id }
      .filter { it.position[axis] < origin }

  return if (pool.none())
    null
  else
    pool
        .firstSortedBy { getNodeDistance(node, it) }
}

private fun moveNodeAlongAxis(a: Int, position: Vector3, radius: Float, nodes: List<Node>, dice: Dice, moveRange: Float): Float {
  val b = (a + 1) % 3
  val c = (a + 2) % 3

  val aCenter = position[a]

  val padding = radius + minInitialNodeDistance

  val bMin = position[b] - padding
  val bMax = position[b] + padding

  val cMin = position[c] - padding
  val cMax = position[c] + padding

  val aligned = nodes.filter {
    it.position[b] + it.radius > bMin &&
        it.position[b] - it.radius < bMax &&
        it.position[c] + it.radius > cMin &&
        it.position[c] - it.radius < cMax
  }

  val minWall = aligned
      .filter {
        it.position[a] < aCenter
      }
      .map { it.position[a] + it.radius }
      .sortedByDescending { it }
      .firstOrNull()

  val maxWall = aligned
      .filter {
        it.position[a] > aCenter
      }
      .map { it.position[a] - it.radius }
      .sortedBy { it }
      .firstOrNull()

  val debugMaxWall = aligned
      .filter {
        it.position[a] > aCenter
      }
      .sortedBy { it.position[a] - it.radius }
      .firstOrNull()

  val freeRangeMin = position[a] - moveRange
  val freeRangeMax = position[a] + moveRange

  val min = if (minWall != null)
    Math.min(
        Math.max(freeRangeMin, minWall + radius + minInitialNodeDistance),
        position[a]
    )
  else
    freeRangeMin

  val max = if (maxWall != null)
    Math.max(
        Math.min(freeRangeMax, maxWall - radius - minInitialNodeDistance),
        position[a]
    )
  else
    freeRangeMax

  assert(max >= min)

  return dice.getFloat(min, max)
}

private fun moveNodes(nodes: MutableList<Node>, moveRange: Float, dice: Dice) {
  val minGap = minInitialNodeDistance + moveRange
  for (i in 0 until nodes.size) {
    val node = nodes[i]
    var position = node.position
//    if (i == 8) {
//      break
//    }
    if (node.id == 23L) {
      val k = 0
    }
    val pool = nodes
        .asSequence()
        .filter { it.id != node.id }
        .filter { withinRangeFast(it.position, position, it.radius + node.radius + minGap) }
        .toList()

//    for (a in 0 until 3) {
    for (a in 0 until 2) {
      val newValue = moveNodeAlongAxis(a, position, node.radius, pool, dice, moveRange)
      position = when (a) {
        0 -> position.copy(x = newValue)
        1 -> position.copy(y = newValue)
        2 -> position.copy(z = newValue)
        else -> throw Error("Not supported")
      }
    }

    nodes[i] = node.copy(position = position)
  }
}

//fun distributeNodes(boundary: WorldBoundary, count: Int, dice: Dice): List<Node> {
//  val cellDimensions = clipDimensions(worldCellLength, boundary.dimensions)
////  cellDimensions.z = 1
//
//  val cellCount = cellDimensions.x * cellDimensions.y * cellDimensions.z
//  val cellChance = (count.toFloat() / cellCount.toFloat())
//  var id = 1L
//
//  val nodes = mutableListOf<Node>()
//
//  for (i in 0 until cellCount) {
//    val position = getPosition(cellDimensions, i) * worldCellLength.toFloat()
//    val chance = cellChance * 6f
//    if (dice.getFloat() < chance) {
//      val node = Node(
//          id = id++,
//          position = position,
//          radius = calculateRadius(position, nodes, dice),
//          isRoom = true
//      )
//      nodes.add(node)
//    }
//  }
//
//  moveNodes(nodes, worldCellLength * 0.5f, dice)
//  return nodes
//}
