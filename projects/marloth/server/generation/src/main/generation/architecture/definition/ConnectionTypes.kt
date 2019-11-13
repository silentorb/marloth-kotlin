package generation.architecture.definition


enum class ConnectionType {
  doorway,
  halfStepOpen,
  open,
  spiralStaircaseBottom,
  spiralStaircaseTop,
  wall
}

// Connections that don't need to be completed and can be left dangling
fun independentConnectionTypes(): Set<ConnectionType> =
    setOf(
        ConnectionType.wall
    )

fun openConnectionTypes(): Set<ConnectionType> =
    setOf(
        ConnectionType.doorway,
        ConnectionType.halfStepOpen,
        ConnectionType.open,
        ConnectionType.spiralStaircaseBottom,
        ConnectionType.spiralStaircaseTop
    )
