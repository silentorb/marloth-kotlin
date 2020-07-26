package generation.architecture.definition

object Connector {
  const val open = "open"
  const val doorway = "doorway"

  const val extraHeadroom = "extraHeadroom"
  const val verticalDiagonal= "verticalDiagonal"
}

val levelConnectors = listOf(
    Connector.open,
    ConnectionType.quarterLevelOpen1,
    ConnectionType.quarterLevelOpen2,
    ConnectionType.quarterLevelOpen3
)

data class LedgeConnector(
    val levelConnector: Any
)

val levelLedgeConnectors = levelConnectors.map { LedgeConnector(it) }
