package generation.elements

import generation.architecture.definition.ConnectionType

val verticalSides = setOf(Direction.up, Direction.down)
val horizontalSideList = listOf(Direction.east, Direction.north, Direction.west, Direction.south)
val horizontalSides = horizontalSideList.toSet()

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

typealias RotatingConnections = Map<ConnectionType, List<ConnectionType>>
