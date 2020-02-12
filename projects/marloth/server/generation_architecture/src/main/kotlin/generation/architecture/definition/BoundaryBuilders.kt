package generation.architecture.definition

import generation.architecture.boundaries.*

fun newHorizontalBoundaryBuilders(): Map<ConnectionType, BoundaryBuilder> = mapOf(
    ConnectionType.doorway to doorwayBoundaryBuilder,
    ConnectionType.decoratedWall to decoratedWallBoundaryBuilder,
    ConnectionType.plainWall to plainWallBoundaryBuilder,
    ConnectionType.solidWall to solidWallBoundaryBuilder
)

fun newVerticalBoundaryBuilders(): Map<ConnectionType, BoundaryBuilder> = mapOf(
)
