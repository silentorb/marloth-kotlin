package generation.architecture.definition

import generation.elements.RotatingConnections


enum class ConnectionType {
  doorway,
  extraHeadroom,
  impassableEmpty,
  quarterLevelOpen1,
  quarterLevelOpen2,
  quarterLevelOpen3,
  open,
  spiralStaircaseBottom,
  spiralStaircaseTop,
  verticalDiagonalAdapter1,
  verticalDiagonalAdapter2,
  verticalDiagonalAdapter3,
  verticalDiagonalAdapter4,
  wall,
  window
}

// Connections that don't need to be completed and can be left dangling
fun independentConnectionTypes(): Set<ConnectionType> =
    setOf(
        ConnectionType.extraHeadroom,
        ConnectionType.impassableEmpty,
        ConnectionType.wall,
        ConnectionType.window
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
