package generation.architecture.definition

import generation.general.ConnectionLogic
import generation.general.Side
import generation.general.newSide

object Sides {
  val flatOpen = newSide(Connector.open)
  val extraHeadroom = newSide(Connector.extraHeadroom)
  val verticalDiagonal = newSide(Connector.verticalDiagonal, connectionLogic = ConnectionLogic.connectWhenPossible)

  val broadOpen = newSide(Connector.open, setOf(
      Connector.open,
      Connector.doorway,
      levelLedgeConnectors.first()
  ))

  val doorway = newSide(Connector.doorway, setOf(Connector.open))
}

fun preferredHorizontalClosed(connector: Any): Side =
    Side(
        connectionLogic = ConnectionLogic.endpointWhenPossible,
        mine = connector,
        other = setOf(connector)
    )
