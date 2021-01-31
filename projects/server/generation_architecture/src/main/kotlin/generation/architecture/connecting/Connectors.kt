package generation.architecture.connecting

object Connector {
  const val open = "open"
  const val doorway = "doorway"
  const val slopeSide = "slopeSide"
  const val solid = "solid"
  const val slopeOverheadWrapper = "slopeOverheadWrapper"
  const val diagonalVerticalSolid = "diagonalVerticalSolid"
  const val headroom = "headroom"
}

data class LevelConnector(
    val connector: Any,
    val level: Int
)

data class SlopeSideConnector(
    val connector: Any,
    val level: Int,
    val alternation: Boolean
)

data class Connectors(
    val open: LevelConnector,
    val doorway: LevelConnector,
    val slopeSides: List<SlopeSideConnector>
)

val levelConnectors: List<Connectors> = (0..3)
    .map {
      Connectors(
          open = LevelConnector(Connector.open, it),
          doorway = LevelConnector(Connector.doorway, it),
          slopeSides = listOf(
              SlopeSideConnector(Connector.slopeSide, it, true),
              SlopeSideConnector(Connector.slopeSide, it, false)
          )
      )
    }
