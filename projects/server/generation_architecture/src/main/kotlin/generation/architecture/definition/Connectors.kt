package generation.architecture.definition

object Connector {
  const val open = "open"
  const val doorway = "doorway"
  const val verticalDiagonal = "verticalDiagonal"
}

data class LevelConnector(
    val connector: Any,
    val level: Int
)

data class Connectors(
    val open: LevelConnector,
    val doorway: LevelConnector
)

val levelConnectors: List<Connectors> = (0..3)
    .map {
      Connectors(
          open = LevelConnector(Connector.open, it),
          doorway = LevelConnector(Connector.doorway, it)
      )
    }

data class LedgeConnector(
    val levelConnector: Any
)

val levelLedgeConnectors = levelConnectors.map { LedgeConnector(it) }
