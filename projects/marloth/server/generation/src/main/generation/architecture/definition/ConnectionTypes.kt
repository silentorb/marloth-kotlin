package generation.architecture.definition


enum class ConnectionType {
  wall,
  doorway,
  spiralStaircase,
  open,
  halfStepOpen
}

// Connections that don't need to be completed and can be left dangling
fun independentConnections(): Set<ConnectionType> =
    setOf(
        ConnectionType.wall
    )

fun closedConnectionTypes(): Set<ConnectionType> =
    setOf(
        ConnectionType.doorway,
        ConnectionType.halfStepOpen,
        ConnectionType.open,
        ConnectionType.spiralStaircase
    )

fun openConnectionTypes(): Set<ConnectionType> =
    ConnectionType
        .values()
        .toSet()
        .minus(closedConnectionTypes())
