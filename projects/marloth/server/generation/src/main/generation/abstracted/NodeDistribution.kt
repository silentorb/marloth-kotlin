package generation.abstracted

import generation.structure.wallHeight
import mythic.spatial.Vector3
import org.joml.Vector3i
import randomly.Dice
import simulation.Biome
import simulation.Node
import simulation.WorldBoundary

private fun clipFloat(unitLength: Int, value: Float): Int =
    (unitLength % value.toInt()) * unitLength

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
  return Vector3(x.toFloat(), y.toFloat(), z.toFloat())
}

fun distributeNodes(boundary: WorldBoundary, count: Int, dice: Dice): List<Node> {
  val cellLength = 10
  val dimensions = clipDimensions(cellLength, boundary.dimensions)
  val dimensionsUnit = Vector3(dimensions.x.toFloat(), dimensions.y.toFloat(), dimensions.z.toFloat())

  val cellCount = dimensions.x * dimensions.y * dimensions.z
  val matrix: MutableList<Node?> = MutableList(cellCount) { null }
  val cellChance = (count.toFloat() / cellCount.toFloat())
  var id = 1L

  val result = mutableListOf<Node>()

  for (i in 0 until cellCount) {
    val position = getPosition(dimensions, i)
    val distanceUnit = (position / dimensionsUnit).length()
    if (distanceUnit > 1f)
      continue

    val falloffModifier = (1f - distanceUnit) * 5f
    val chance = cellChance * falloffModifier
    if (dice.getFloat() < chance) {
      val node = Node(
          id = id++,
          position = position,
          radius = 1f,
          isSolid = false,
          isWalkable = true,
          biome = Biome.void,
          height = wallHeight
      )
      matrix[i] = node
      result.add(node)
    }
  }

  return result
}