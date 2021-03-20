package generation.architecture.connecting

import generation.general.ConnectionLogic
import generation.general.Side

object Sides {
  val slopeOctaveWrap = Side()
  val solid = Side(isTraversable = false)
  val solidRequired = solid.copy()
  val solidDiagonalVertical = Side(isTraversable = false)
  val solidDiagonalVerticalRequired = solidDiagonalVertical.copy()
  val headroomHorizontal = Side(isTraversable = false
  )
  val headroomVertical = headroomHorizontal.copy(
  )
}

data class LevelSides(
    val open: Side,
    val openRequired: Side,
    val doorway: Side,
    val slopeSides: List<Side>
)

fun newSlopeSide(level: Int, alternation: Int) =
    Side()

val levelSides: List<LevelSides> = (0..3)
    .map { level ->
      val connectors = levelConnectors[level]
      val open = connectors.open
      val doorway = connectors.doorway
      val openSide = Side()
      LevelSides(
          open = openSide,
          openRequired = openSide.copy(
          ),
          doorway = Side(),
          slopeSides = listOf(
              newSlopeSide(level, 0),
              newSlopeSide(level, 1)
          )
      )
    }

fun uniqueConnection(prefix: String): Pair<Side, Side> {
  val firstConnector = prefix + "1"
  val secondConnector = prefix + "2"
  return Pair(
      Side(),
      Side()
  )
}
