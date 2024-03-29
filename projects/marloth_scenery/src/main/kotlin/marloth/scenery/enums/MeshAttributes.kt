package marloth.scenery.enums

import silentorb.mythic.scenery.MeshName
import silentorb.mythic.scenery.Shape

//typealias MeshAttributes = Set<MeshAttribute>

data class ArchitectureMeshInfo(
    val shape: Shape?
)

typealias MeshInfoMap = Map<MeshName, ArchitectureMeshInfo>

typealias MeshShapeMap = Map<MeshName, Shape>

//typealias MeshAttributeMap = Map<MeshName, MeshAttributes>

//enum class MeshAttribute {
//  canHaveAttachment,
//
//  // Rotation
//  canFlipHorizontally,
//  canFlipUpsideDown,
//  canRotateOnSide,
//
//  decorated,
//  doorway,
//  nonSolid,
//  plain,
//  solid,
//
//  heightFull,
//  heightHalf,
//  heightThreeQuarters,
//  heightQuarter,
//
//  wall,
//}

//val meshAttributesThatRequireAShape = setOf(
//    MeshAttribute.doorway,
//    MeshAttribute.solid,
//    MeshAttribute.wall
//)

//val meshAttributes: MeshAttributeMap = mapOf(
//    MeshId.circleFloor to setOf(
//    ),
//    MeshId.curvingStairStep to setOf(
//    ),
//    MeshId.fillerColumn to setOf(
//    ),
//    MeshId.halfCircleFloor to setOf(
//    ),
//    MeshId.halfSquareFloor to setOf(
//
//    ),
//    MeshId.longStairStep to setOf(
//    ),
//    MeshId.longStep to setOf(
//    ),
//    MeshId.quarterSlope to setOf(
//    ),
//    MeshId.squareFloor to setOf(
//    ),
//    MeshId.squareFloorHalfDiagonal to setOf(
//    ),
//    MeshId.squareWall to setOf(
//        MeshAttribute.wall,
//        MeshAttribute.canHaveAttachment,
//        MeshAttribute.heightFull,
//        MeshAttribute.plain
//    ),
//    MeshId.squareWallDoorway to setOf(
//        MeshAttribute.doorway
//    ),
//    MeshId.squareWallQuarterHeight to setOf(
//    ),
//    MeshId.squareWallQuarterSlope to setOf(
//    ),
//    MeshId.squareWallWindow to setOf(
//        MeshAttribute.decorated,
//        MeshAttribute.nonSolid,
//        MeshAttribute.wall
//    )
//)
//    .mapKeys { it.key }
