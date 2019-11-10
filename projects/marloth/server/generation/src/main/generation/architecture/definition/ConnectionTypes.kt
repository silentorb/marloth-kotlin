package generation.architecture.definition


enum class ConnectionType {
  impassable,
  doorway,
  spiralStaircase,
  open
}

fun closedConnectionTypes(): Set<ConnectionType> =
    setOf(
        ConnectionType.doorway,
        ConnectionType.open,
        ConnectionType.spiralStaircase
    )

fun openConnectionTypes(): Set<ConnectionType> =
    ConnectionType
        .values()
        .toSet()
        .minus(closedConnectionTypes())
