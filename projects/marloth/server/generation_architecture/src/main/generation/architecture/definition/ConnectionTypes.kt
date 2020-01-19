package generation.architecture.definition

import generation.general.RotatingConnections

enum class ConnectionType {
  cylinderWall,
  decoratedWall,
  doorway,
  extraHeadroom,
  impassableEmpty,
  open,
  quarterLevelOpen1,
  quarterLevelOpen2,
  quarterLevelOpen3,
  spiralStaircaseBottom,
  spiralStaircaseTop,
  verticalDiagonalAdapter1,
  verticalDiagonalAdapter2,
  verticalDiagonalAdapter3,
  verticalDiagonalAdapter4,
  plainWall,
  solidWall,
}

// Connections that don't need to be completed and can be left dangling
fun independentConnectionTypes(): Set<ConnectionType> =
    setOf(
        ConnectionType.cylinderWall,
        ConnectionType.extraHeadroom,
        ConnectionType.impassableEmpty,
        ConnectionType.plainWall,
        ConnectionType.solidWall,
        ConnectionType.decoratedWall
    )

fun openConnectionTypes(): Set<ConnectionType> =
    setOf(
        ConnectionType.doorway,
        ConnectionType.quarterLevelOpen1,
        ConnectionType.quarterLevelOpen2,
        ConnectionType.quarterLevelOpen3,
        ConnectionType.open,
        ConnectionType.spiralStaircaseBottom,
        ConnectionType.spiralStaircaseTop,
        ConnectionType.verticalDiagonalAdapter1,
        ConnectionType.verticalDiagonalAdapter2,
        ConnectionType.verticalDiagonalAdapter3,
        ConnectionType.verticalDiagonalAdapter4
    )

fun rotatingConnectionTypes(): RotatingConnections = mapOf(
    ConnectionType.verticalDiagonalAdapter1 to listOf(
        ConnectionType.verticalDiagonalAdapter1,
        ConnectionType.verticalDiagonalAdapter2,
        ConnectionType.verticalDiagonalAdapter3,
        ConnectionType.verticalDiagonalAdapter4
    )
)
