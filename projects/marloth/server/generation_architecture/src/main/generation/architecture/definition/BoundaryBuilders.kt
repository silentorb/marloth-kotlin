package generation.architecture.definition

import generation.architecture.boundaries.wallBoundaryBuilder
import generation.architecture.misc.BoundaryBuilder

fun newBoundaryBuilders(): Map<ConnectionType, BoundaryBuilder> = mapOf(
    ConnectionType.plainWall to wallBoundaryBuilder
)
