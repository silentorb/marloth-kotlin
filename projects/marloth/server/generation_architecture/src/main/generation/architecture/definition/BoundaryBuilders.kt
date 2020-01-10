package generation.architecture.definition

import generation.architecture.boundaries.wallBoundaryBuilder
import generation.architecture.misc.BoundaryBuilder

fun newHorizontalBoundaryBuilders(): Map<ConnectionType, BoundaryBuilder> = mapOf(
    ConnectionType.plainWall to wallBoundaryBuilder
)

fun newVerticalBoundaryBuilders(): Map<ConnectionType, BoundaryBuilder> = mapOf(
)
