package generation.architecture.building

import generation.architecture.cellLength
import generation.architecture.definition.*
import generation.elements.Side
import mythic.spatial.Vector3
import scenery.enums.MeshId

const val quarterStep = cellLength / 4f

private data class Level(
    val height: Float,
    val side: Side
)

private val levels = listOf(
    Level(quarterStep * 0, requiredOpen),
    Level(quarterStep * 1, setOf(ConnectionType.quarterLevelOpen1)),
    Level(quarterStep * 2, setOf(ConnectionType.quarterLevelOpen2)),
    Level(quarterStep * 3, setOf(ConnectionType.quarterLevelOpen3))
)

private fun newSlope(lower: Level, higherSide: Side, upSide: Side = extraHeadroom) =
    compose(
        blockBuilder(
            up = upSide,
            east = higherSide,
            north = impassableHorizontalSolid,
            west = lower.side,
            south = impassableHorizontalSolid
        ),
        newSlopedFloorMesh(MeshId.quarterSlope.name, lower.height),
        cubeWalls()
    )

fun explodeHeightBlocks(levelIndex: Int): Collection<BlockBuilder> {
  val level = levels[levelIndex]
  val lower = levels[levelIndex - 1]
  val upper = levels[(levelIndex + 1) % 4]
  val halfStepRequiredOpen: Side = level.side
  val halfStepOptionalOpen: Side = halfStepRequiredOpen.plus(ConnectionType.wall)
  return mapOf(
      "halfStepRoom" to compose(
          blockBuilder(
              up = extraHeadroom,
              east = halfStepOptionalOpen,
              north = halfStepOptionalOpen,
              west = halfStepOptionalOpen,
              south = halfStepOptionalOpen
          ),
          floorMesh(MeshId.squareFloor.name, Vector3(0f, 0f, level.height)),
          cubeWalls()
      ),

      "lowerHalfStepSlope" to newSlope(lower, level.side),

      "diagonalCorner" to diagonalCornerFloor(level.side, level.height)
  )
      .values
}

fun heights(): Collection<BlockBuilder> =
    (1..3)
        .flatMap(::explodeHeightBlocks)
        .plus(newSlope(levels.last(), impassableHorizontal, verticalDiagonalAdapter))
