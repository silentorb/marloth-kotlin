package generation.general

val verticalSides = setOf(Direction.up, Direction.down)

typealias Side = Set<Any>

fun isSideOpen(openConnections: Set<Any>, side: Side): Boolean =
    side.any { openConnections.contains(it) }
