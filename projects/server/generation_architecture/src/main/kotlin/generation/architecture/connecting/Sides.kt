package generation.architecture.connecting

import generation.general.ConnectionLogic
import generation.general.Side

object Sides {
  val slopeOctaveWrap = Side(Connector.slopeOverheadWrapper, connectionLogic = ConnectionLogic.required)
  val solid = Side(Connector.solid, isTraversable = false)
  val solidRequired = solid.copy(connectionLogic = ConnectionLogic.required)
  val solidDiagonalVertical = Side(Connector.diagonalVerticalSolid, isTraversable = false)
  val solidDiagonalVerticalRequired = solidDiagonalVertical.copy(connectionLogic = ConnectionLogic.required)
  val headroomHorizontal = Side(Connector.headroom,
      isTraversable = false,
      connectionLogic = ConnectionLogic.minimal,
      isUniversal = true
  )
  val headroomVertical = headroomHorizontal.copy(
      connectionLogic = ConnectionLogic.required
  )
}

data class LevelSides(
    val open: Side,
    val openRequired: Side,
    val doorway: Side,
    val slopeSides: List<Side>
)

fun newSlopeSide(level: Int, alternation: Int) =
    Side(
        levelConnectors[level].slopeSides[alternation],
        setOf(levelConnectors[level].slopeSides[1 - alternation])
    )

val levelSides: List<LevelSides> = (0..3)
    .map { level ->
      val connectors = levelConnectors[level]
      val open = connectors.open
      val doorway = connectors.doorway
      val openSide = Side(open, setOf(
          open,
          doorway
      ))
      LevelSides(
          open = openSide,
          openRequired = openSide.copy(
              connectionLogic = ConnectionLogic.required
          ),
          doorway = Side(doorway, setOf(open)),
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
      Side(
          firstConnector, setOf(secondConnector),
          connectionLogic = ConnectionLogic.required
      ),
      Side(
          secondConnector, setOf(firstConnector),
          connectionLogic = ConnectionLogic.required
      )
  )
}
