package generation.architecture.building

import simulation.misc.cellLength
import generation.architecture.definition.*
import generation.architecture.misc.squareOffsets
import generation.general.Block
import generation.general.Side
import generation.general.newSide
import silentorb.mythic.spatial.Vector3
import marloth.scenery.enums.MeshId
import simulation.misc.CellAttribute
import simulation.misc.floorOffset

const val quarterStep = cellLength / 4f

private data class Level(
    val height: Float,
    val side: Side,
    val up: Side
)

//private val levels = listOf(
//    Level(quarterStep * 0, requiredWideOpen, impassableVertical),
//    Level(quarterStep * 1, newSide(ConnectionType.quarterLevelOpen1), impassableVertical),
//    Level(quarterStep * 2, newSide(ConnectionType.quarterLevelOpen2), impassableVertical),
//    Level(quarterStep * 3, newSide(ConnectionType.quarterLevelOpen3), extraHeadroom)
//)
//
//private fun newSlope(lower: Level, upper: Level) =
//    BlockBuilder(
//        block = Block(
//            sides = sides(
//                up = upper.up,
//                east = upper.side,
//                north = impassableHorizontalSolid,
//                west = lower.side,
//                south = impassableHorizontalSolid
//            ),
//            attributes = setOf(CellAttribute.traversable)
//        )
//    ) + newSlopedFloorMesh(MeshId.quarterSlope, lower.height)
//
//fun plainSlopeSlot(lowerHeight: Float) =
//    BlockBuilder(
//        block = Block(
//            slots = listOf(Vector3(0f, 0f, lowerHeight + quarterStep / 2f + 0.05f) + floorOffset)
//        )
//    )
//
//fun explodeHeightBlocks(levelIndex: Int): Map<String, BlockBuilder> {
//  val upper = levels[levelIndex]
//  val lower = levels[levelIndex - 1]
//  val halfStepRequiredOpen: Side = upper.side
//  val halfStepOptionalOpen: Side = halfStepRequiredOpen //.plus(ConnectionType.plainWall)
//  return mapOf(
//      "halfStepRoom$levelIndex" to BlockBuilder(
//          block = Block(
//              sides = sides(
//                  up = extraHeadroom,
//                  east = halfStepOptionalOpen,
//                  north = halfStepOptionalOpen,
//                  west = halfStepOptionalOpen,
//                  south = halfStepOptionalOpen
//              ),
//              attributes = setOf(CellAttribute.traversable),
//              slots = squareOffsets(2).map { it + Vector3(0f, 0f, upper.height) }
//          )
//      ) + floorMesh(MeshId.squareFloor, Vector3(0f, 0f, upper.height)) +
//          cubeWallLamps(lampRate = 0.5f, heightOffset = upper.height - 0.1f)
//      ,
//
//      "lowerHalfStepSlopeA$levelIndex" to newSlope(lower, upper) +
//          plainSlopeSlot(lower.height),
//
//      "lowerHalfStepSlopeB$levelIndex" to newSlope(lower, upper) +
//          newSlopeEdgeBlock(MeshId.largeBrick, lower.height + quarterStep + quarterStep, upper.side, -1),
//
//      "lowerHalfStepSlopeC$levelIndex" to newSlope(lower, upper) +
//          newSlopeEdgeBlock(MeshId.largeBrick, lower.height + quarterStep + quarterStep, upper.side, 1),
//
//      "diagonalCorner$levelIndex" to diagonalCornerFloor(upper.height) + BlockBuilder(
//          block = Block(
//              sides = sides(
//                  up = upper.up,
//                  east = upper.side,
//                  north = upper.side
//              )
//          )
//      )
//  )
//}

//fun heights(): Map<String, BlockBuilder> =
//    (1..3)
//        .map(::explodeHeightBlocks)
//        .reduce { a, b -> a.plus(b) }
//        .plus("lowerHalfStepSlope4" to newSlope(levels.last(), Level(0f, impassableHorizontal, verticalDiagonalAdapter)))
