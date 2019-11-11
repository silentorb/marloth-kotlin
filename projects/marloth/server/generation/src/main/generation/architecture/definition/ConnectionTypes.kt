package generation.architecture.definition


enum class ConnectionType {
  impassable,
  doorway,
  spiralStaircase,
  open,
  halfStepOpen
}

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
