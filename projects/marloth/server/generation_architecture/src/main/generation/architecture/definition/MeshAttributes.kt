package generation.architecture.definition

import generation.architecture.misc.MeshAttributeMap
import scenery.enums.MeshId

enum class MeshAttribute {
    canHaveAttachment,

    // Rotation
    canFlipHorizontally,
    canFlipUpsideDown,
    canRotateOnSide,

    doorway,
    hasDecoration,

    heightFull,
    heightHalf,
    heightThreeQuarters,
    heightQuarter,

    wall,
}

val meshAttributes: MeshAttributeMap = mapOf(
    MeshId.circleFloor to setOf(
    ),
    MeshId.curvingStairStep to setOf(
    ),
    MeshId.fillerColumn to setOf(
    ),
    MeshId.halfCircleFloor to setOf(
    ),
    MeshId.longStairStep to setOf(
    ),
    MeshId.longStep to setOf(
    ),
    MeshId.squareFloor to setOf(
    ),
    MeshId.squareWall to setOf(
        MeshAttribute.wall,
        MeshAttribute.canHaveAttachment,
        MeshAttribute.heightFull
    )
)
    .mapKeys { it.key.name }
