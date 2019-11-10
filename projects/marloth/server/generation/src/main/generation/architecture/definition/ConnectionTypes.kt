package generation.architecture.definition


enum class ConnectionType {
  impassable,
  doorway,
  self,
  open
}

fun closedConnectionTypes(): Set<ConnectionType> =
    setOf(
        ConnectionType.doorway,
        ConnectionType.open
    )

fun openConnectionTypes(): Set<ConnectionType> =
    ConnectionType
        .values()
        .toSet()
        .minus(closedConnectionTypes())
