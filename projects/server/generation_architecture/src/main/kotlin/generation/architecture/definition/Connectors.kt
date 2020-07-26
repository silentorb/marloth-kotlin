package generation.architecture.definition

object Connector {
  const val open = "open"
  const val doorway = "doorway"

  const val extraHeadroom = "extraHeadroom"
  const val verticalDiagonal= "verticalDiagonal"
  const val quarterLevelOpen1 = "quarterLevelOpen1"
  const val quarterLevelOpen2 = "quarterLevelOpen2"
  const val quarterLevelOpen3 = "quarterLevelOpen3"
}

val levelConnectors = listOf(
    Connector.open,
    Connector.quarterLevelOpen1,
    Connector.quarterLevelOpen2,
    Connector.quarterLevelOpen3
)

data class LedgeConnector(
    val levelConnector: Any
)

val levelLedgeConnectors = levelConnectors.map { LedgeConnector(it) }
