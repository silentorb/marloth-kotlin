package generation.architecture.building

import simulation.misc.cellLength
import generation.architecture.definition.*
import generation.architecture.misc.squareOffsets
import generation.general.Block
import generation.general.Side
import silentorb.mythic.spatial.Vector3
import marloth.scenery.enums.MeshId
import silentorb.mythic.scenery.MeshName
import simulation.misc.CellAttribute

const val quarterStep = cellLength / 4f

private data class Level(
    val height: Float,
    val side: Side,
    val up: Side
)

private val levels = listOf(
    Level(quarterStep * 0, requiredWideOpen, impassableVertical),
    Level(quarterStep * 1, setOf(ConnectionType.quarterLevelOpen1), impassableVertical),
    Level(quarterStep * 2, setOf(ConnectionType.quarterLevelOpen2), impassableVertical),
    Level(quarterStep * 3, setOf(ConnectionType.quarterLevelOpen3), extraHeadroom)
)

private fun newSlope(lower: Level, upper: Level) =
    BlockBuilder(
        block = Block(
            attributes = setOf(CellAttribute.traversable),
            sides = sides(
                up = upper.up,
                east = upper.side,
                north = impassableHorizontalSolid,
                west = lower.side,
                south = impassableHorizontalSolid
            )
        )
    ) + newSlopedFloorMesh(MeshId.quarterSlope.name, lower.height) //+

fun explodeHeightBlocks(levelIndex: Int): Map<String, BlockBuilder> {
  val upper = levels[levelIndex]
  val lower = levels[levelIndex - 1]
  val halfStepRequiredOpen: Side = upper.side
  val halfStepOptionalOpen: Side = halfStepRequiredOpen.plus(ConnectionType.plainWall)
  return mapOf(
      "halfStepRoom$levelIndex" to BlockBuilder(
          block = Block(
              attributes = setOf(CellAttribute.traversable),
              slots = squareOffsets(2).map { it + upper.height },
              sides = sides(
                  up = extraHeadroom,
                  east = halfStepOptionalOpen,
                  north = halfStepOptionalOpen,
                  west = halfStepOptionalOpen,
                  south = halfStepOptionalOpen
              )
          )
      ) + floorMesh(MeshId.squareFloor.name, Vector3(0f, 0f, upper.height)) +
          cubeWallLamps(lampRate = 0.5f, heightOffset = upper.height - 0.1f)
      ,

      "lowerHalfStepSlopeA$levelIndex" to newSlope(lower, upper),

      "lowerHalfStepSlopeB$levelIndex" to newSlope(lower, upper) +
          newSlopeEdgeBlock(MeshId.largeBrick.name, lower.height + quarterStep + quarterStep, upper.side, -1),

      "lowerHalfStepSlopeC$levelIndex" to newSlope(lower, upper) +
          newSlopeEdgeBlock(MeshId.largeBrick.name, lower.height + quarterStep + quarterStep, upper.side, 1),

      "diagonalCorner$levelIndex" to diagonalCornerFloor(upper.height) + BlockBuilder(
          block = Block(
              sides = sides(
                  up = upper.up,
                  east = upper.side,
                  north = upper.side
              )
          )
      )
  )
}

fun heights(): Map<String, BlockBuilder> =
    (1..3)
        .map(::explodeHeightBlocks)
        .reduce { a, b -> a.plus(b) }
        .plus("lowerHalfStepSlope4" to newSlope(levels.last(), Level(0f, impassableHorizontal, verticalDiagonalAdapter)))
