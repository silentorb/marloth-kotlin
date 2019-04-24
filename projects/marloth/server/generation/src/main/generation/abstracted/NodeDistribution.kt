package generation.abstracted

import generation.getNodeDistance
import generation.structure.wallHeight
import mythic.ent.firstSortedBy
import mythic.spatial.Vector3
import mythic.spatial.withinRangeFast
import org.joml.Vector3i
import randomly.Dice
import simulation.Biome
import simulation.Node
import simulation.WorldBoundary

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
    it.position[a] < aCenter &&
        it.position[b] + it.radius > bMin &&
        it.position[b] - it.radius < bMax &&
        it.position[c] + it.radius > cMin &&
        it.position[c] - it.radius < cMax
  }
      .map { it.position[a] }

  val minWall = aligned.filter {
    it < aCenter
  }
      .sortedByDescending { it }
      .firstOrNull()

  val maxWall = aligned.filter {
    it > aCenter
  }
      .sortedBy { it }
      .firstOrNull()

  val freeRangeMin = position[a] - moveRange
  val freeRangeMax = position[a] + moveRange

  val min = if (minWall != null)
    Math.max(freeRangeMin, minWall)
  else
    freeRangeMin

  val max = if (maxWall != null)
    Math.min(freeRangeMax, maxWall)
  else
    freeRangeMax

  return dice.getFloat(min, max)
}

private fun moveNodes(nodes: MutableList<Node>, moveRange: Float, dice: Dice) {
  val minGap = minInitialNodeDistance + moveRange
  for (i in 0 until nodes.size) {
    val node = nodes[i]
    var position = node.position

    val pool = nodes
        .asSequence()
        .filter { it.id != node.id }
        .filter { withinRangeFast(it.position, position, it.radius + node.radius + minGap) }
        .toList()

    for (a in 0 until 3) {
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

fun distributeNodes(boundary: WorldBoundary, count: Int, dice: Dice): List<Node> {
  val cellLength = 20
  val cellDimensions = clipDimensions(cellLength, boundary.dimensions)
  val dimensionsUnit = Vector3(
      cellDimensions.x.toFloat(),
      cellDimensions.y.toFloat(),
      cellDimensions.z.toFloat()
  ) * cellLength.toFloat() * 0.5f

  val cellCount = cellDimensions.x * cellDimensions.y * cellDimensions.z
//  val matrix: MutableList<Long?> = MutableList(cellCount) { null }
  val cellChance = (count.toFloat() / cellCount.toFloat())
  var id = 1L

  val nodes = mutableListOf<Node>()

  for (i in 0 until cellCount) {
    val position = getPosition(cellDimensions, i) * cellLength.toFloat()
    val distanceUnit = (position / dimensionsUnit).length()
    if (distanceUnit > 1f)
      continue

    val falloffModifier = (1f - distanceUnit)
    val chance = cellChance * falloffModifier * 6f
    if (dice.getFloat() < chance) {
      val node = Node(
          id = id++,
          position = position,
          radius = calculateRadius(position, nodes, dice),
          isSolid = false,
          isWalkable = true,
          biome = Biome.void,
          height = wallHeight
      )
//      matrix[i] = node.id
      nodes.add(node)
    }
  }

  moveNodes(nodes, cellLength * 0.5f, dice)
  return nodes
}