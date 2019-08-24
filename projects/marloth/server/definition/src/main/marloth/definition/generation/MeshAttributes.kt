package marloth.definition.generation

import generation.misc.MeshAttribute
import generation.misc.MeshAttributeMap
import scenery.enums.MeshId

val meshAttributes: MeshAttributeMap = mapOf(
    MeshId.circleFloor to setOf(
        MeshAttribute.placementShortFloor
    ),
    MeshId.longPillowStep to setOf(
        MeshAttribute.placementStairStep
    ),
    MeshId.longStairStep to setOf(
        MeshAttribute.placementStairStep
    ),
    MeshId.longStep to setOf(
        MeshAttribute.placementTunnelFloor
    ),
    MeshId.pillowWall to setOf(
        MeshAttribute.placementWall
    ),
    MeshId.squareFloor to setOf(
        MeshAttribute.placementShortFloor
    ),
    MeshId.squareWall to setOf(
        MeshAttribute.placementWall,
        MeshAttribute.canHaveAttachment
    ),
    MeshId.threeStoryCircleFloor to setOf(
        MeshAttribute.placementTallFloor
    ),
    MeshId.windowWall to setOf(
        MeshAttribute.placementWindow
    )
)
    .mapKeys { it.key.name }
