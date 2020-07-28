package generation.architecture.definition

import generation.general.ConnectionLogic
import generation.general.Side
import generation.general.newSide

object Sides {
  val slopeOctaveWrap = newSide(Connector.verticalDiagonal, connectionLogic = ConnectionLogic.required)
  val doorway = newSide(Connector.doorway, setOf(Connector.open))
}

data class LevelSides(
    val open: Side,
    val doorway: Side
)

val levelSides: List<LevelSides> = (0..3)
    .map { index ->
      val connectors = levelConnectors[index]
      val open = connectors.open
      val doorway = connectors.doorway
      LevelSides(
          open = newSide(open, setOf(
              open,
              doorway
          )),
          doorway = newSide(doorway, setOf(open))
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
