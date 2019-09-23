package marloth.definition.generation

import generation.misc.MeshAttribute
import generation.misc.MeshAttributeMap
import scenery.enums.MeshId

val meshAttributes: MeshAttributeMap = mapOf(
    MeshId.circleFloor to setOf(
        MeshAttribute.placementShortRoomFloor,
        MeshAttribute.placementRoomCeiling
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
        MeshAttribute.placementShortRoomFloor
    ),
    MeshId.squareWall to setOf(
        MeshAttribute.placementWall,
        MeshAttribute.canHaveAttachment
    ),
    MeshId.threeStoryCircleFloor to setOf(
        MeshAttribute.placementTallRoomFloor
    ),
    MeshId.windowWall to setOf(
        MeshAttribute.placementWindow
    )
)
    .mapKeys { it.key.name }
