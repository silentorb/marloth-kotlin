package generation.architecture.definition

import generation.general.newSide

object Sides {
  val flatOpen = newSide(Connector.open)
  val extraHeadroom = newSide(Connector.extraHeadroom)
  val verticalDiagonal = newSide(Connector.verticalDiagonal)

  val broadOpen = newSide(Connector.open, setOf(
      Connector.open,
      Connector.doorway,
      levelLedgeConnectors.first()
  ))

  val doorway = newSide(Connector.doorway, setOf(Connector.open))
}
