package generation.architecture.definition

import generation.general.ConnectionLogic
import generation.general.Side
import generation.general.newSide

object Sides {
  val slopeOctaveWrap = newSide(Connector.verticalDiagonal, connectionLogic = ConnectionLogic.required)
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
      val openSide = newSide(open, setOf(
          open,
          doorway
      ))
      LevelSides(
          open = openSide,
          openRequired = openSide.copy(
              connectionLogic = ConnectionLogic.required
          ),
          doorway = newSide(doorway, setOf(open)),
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
