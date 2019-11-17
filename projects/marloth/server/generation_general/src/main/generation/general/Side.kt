package generation.general

val verticalSides = setOf(Direction.up, Direction.down)

typealias Side = Set<Any>

val oppositeDirections: Map<Direction, Direction> = mapOf(
    Direction.up to Direction.down,
    Direction.down to Direction.up,
    Direction.west to Direction.east,
    Direction.east to Direction.west,
    Direction.north to Direction.south,
    Direction.south to Direction.north
)

fun isSideOpen(openConnections: Set<Any>, side: Side): Boolean =
    side.any { openConnections.contains(it) }
