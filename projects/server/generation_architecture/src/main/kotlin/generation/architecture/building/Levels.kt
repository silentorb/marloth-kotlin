package generation.architecture.building

import simulation.misc.cellLength
import generation.architecture.definition.*
import generation.architecture.misc.squareOffsets
import generation.general.Block
import generation.general.Direction
import generation.general.Side
import silentorb.mythic.spatial.Vector3
import marloth.scenery.enums.MeshId
import simulation.misc.CellAttribute

const val quarterStep = cellLength / 4f

private data class Level(
    val height: Float,
    val side: Side
)

private val levels = listOf(
    Level(quarterStep * 0, requiredWideOpen),
    Level(quarterStep * 1, setOf(ConnectionType.quarterLevelOpen1)),
    Level(quarterStep * 2, setOf(ConnectionType.quarterLevelOpen2)),
    Level(quarterStep * 3, setOf(ConnectionType.quarterLevelOpen3))
)

private fun newSlope(lower: Level, higherSide: Side, upSide: Side = extraHeadroom) =
    BlockBuilder(
        block = Block(
            attributes = setOf(CellAttribute.traversable),
            sides = sides(
                up = upSide,
                east = higherSide,
                north = impassableHorizontalSolid,
                west = lower.side,
                south = impassableHorizontalSolid
            )
        )
    ) + newSlopedFloorMesh(MeshId.quarterSlope.name, lower.height) //+
//        newSlopeEdgeBlock(MeshId.largeBrick.name, lower.height)

fun explodeHeightBlocks(levelIndex: Int): Map<String, BlockBuilder> {
  val level = levels[levelIndex]
  val lower = levels[levelIndex - 1]
  val upper = levels[(levelIndex + 1) % 4]
  val halfStepRequiredOpen: Side = level.side
  val halfStepOptionalOpen: Side = halfStepRequiredOpen.plus(ConnectionType.plainWall)
  return mapOf(
      "halfStepRoom$levelIndex" to BlockBuilder(
          block = Block(
              attributes = setOf(CellAttribute.traversable),
              slots = squareOffsets(2).map { it + level.height },
              sides = sides(
                  up = extraHeadroom,
                  east = halfStepOptionalOpen,
                  north = halfStepOptionalOpen,
                  west = halfStepOptionalOpen,
                  south = halfStepOptionalOpen
              )
          )
      ) + floorMesh(MeshId.squareFloor.name, Vector3(0f, 0f, level.height)) +
//          cubeWalls(height = level.height),
          cubeWallLamps(lampRate = 0.5f, heightOffset = level.height - 0.1f)
      ,

//      "lowerHalfStepSlopeA$levelIndex" to newSlope(lower, level.side) +
//          newSlopeEdgeBlock(MeshId.largeBrick.name, lower.height + quarterStep + quarterStep, level.side, -1),

      "lowerHalfStepSlopeB$levelIndex" to newSlope(lower, level.side) +
          newSlopeEdgeBlock(MeshId.largeBrick.name, lower.height + quarterStep + quarterStep, level.side, 1),

      "diagonalCorner$levelIndex" to diagonalCornerFloor(level.side, level.height)
  )
}

fun heights(): Map<String, BlockBuilder> =
    (1..3)
        .map(::explodeHeightBlocks)
        .reduce { a, b -> a.plus(b) }
        .plus("lowerHalfStepSlope4" to newSlope(levels.last(), impassableHorizontal, verticalDiagonalAdapter))
