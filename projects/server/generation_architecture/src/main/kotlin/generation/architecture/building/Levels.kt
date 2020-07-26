package generation.architecture.building

import simulation.misc.cellLength
import generation.architecture.definition.*
import generation.architecture.misc.squareOffsets
import generation.architecture.old.getTurnDirection
import generation.general.Block
import generation.general.Side
import generation.general.endpoint
import generation.general.newSide
import silentorb.mythic.spatial.Vector3
import marloth.scenery.enums.MeshId
import simulation.misc.CellAttribute
import simulation.misc.floorOffset

const val quarterStep = cellLength / 4f

data class Level(
    val index: Int,
    val side: Side,
    val up: Side
) {
  val height: Float = index.toFloat() * quarterStep
}

fun newLevel(index: Int, connector: Any, up: Side, additionalConnectors: Set<Any>) =
    Level(index, newSide(connector, additionalConnectors + connector), up)

data class SlopeSideConnector(
    val level: Int,
    val alternation: Boolean
)

fun newSlopeSide(level: Int, alternation: Boolean) =
    Side(
        SlopeSideConnector(level, alternation),
        setOf(SlopeSideConnector(level, !alternation))
    )

private val levels = listOf(
    newLevel(0, Connector.open, endpoint, setOf(levelLedgeConnectors[0])),
    newLevel(1, ConnectionType.quarterLevelOpen1, endpoint, setOf(levelLedgeConnectors[1])),
    newLevel(2, ConnectionType.quarterLevelOpen2, endpoint, setOf(levelLedgeConnectors[2])),
    newLevel(3, ConnectionType.quarterLevelOpen3, Sides.extraHeadroom, setOf(levelLedgeConnectors[3]))
)

fun slopeSides(lower: Level, upper: Level) =
    sides(
        up = upper.up,
        east = upper.side,
        west = lower.side,
        north = newSlopeSide(lower.index, true),
        south = newSlopeSide(lower.index, false)
    )

fun newSlope(lower: Level, upper: Level) =
    BlockBuilder(
        block = Block(
            sides = slopeSides(lower, upper),
            attributes = setOf(CellAttribute.traversable)
        ),
        builder = mergeBuilders(
            newSlopedFloorMesh(MeshId.quarterSlope, lower.height),
            placeCubeRoomWalls(MeshId.squareWall)
        )
    )

fun plainSlopeSlot(lowerHeight: Float) =
    BlockBuilder(
        block = Block(
            slots = listOf(Vector3(0f, 0f, lowerHeight + quarterStep / 2f + 0.05f) + floorOffset)
        )
    )

fun newLedgeSide(level: Int) =
    newSide(levelLedgeConnectors[level], setOf(levelConnectors[level]), true)

fun newLedgeSlope(lower: Level, upper: Level, name: String, ledgeTurns: Int): BlockBuilder {
  val height = lower.height + quarterStep + quarterStep
  return BlockBuilder(
      block = Block(
          name = name,
          sides = sides(
              up = upper.up,
              east = newLedgeSide(upper.index),
              west = newLedgeSide(lower.index),
              north = newSlopeSide(lower.index, true),
              south = newSlopeSide(lower.index, false)
          ) + mapOf(
              getTurnDirection(ledgeTurns) to upper.side
          ),
          slots = listOf(
//            Vector3(cellLength * (0.5f - ledgeTurns.toFloat() * 0.25f), cellLength * 0.25f, height)
              Vector3(cellLength * 0.25f, cellLength * (0.5f + ledgeTurns.toFloat() * 0.25f), height - quarterStep)
          ),
          attributes = setOf(CellAttribute.traversable)
      ),
      builder = mergeBuilders(
          newSlopedFloorMesh(MeshId.quarterSlope, lower.height),
          newSlopeEdgeBlock(MeshId.largeBrick, lower.height + quarterStep + quarterStep, ledgeTurns),
          placeCubeRoomWalls(MeshId.squareWall)
      )
  )
}

fun explodeHeightBlocks(levelIndex: Int): List<BlockBuilder> {
  val upper = levels[levelIndex]
  val lower = levels[levelIndex - 1]
  val halfStepRequiredOpen: Side = upper.side
  val halfStepOptionalOpen: Side = halfStepRequiredOpen //.plus(ConnectionType.plainWall)
  return listOf(
      BlockBuilder(
          block = Block(
              name = "halfStepRoom$levelIndex",
              sides = sides(
                  up = Sides.extraHeadroom,
                  east = halfStepOptionalOpen,
                  north = halfStepOptionalOpen,
                  west = halfStepOptionalOpen,
                  south = halfStepOptionalOpen
              ),
              attributes = setOf(CellAttribute.traversable),
              slots = squareOffsets(2).map { it + Vector3(0f, 0f, upper.height) }
          ),
          builder = mergeBuilders(
              floorMesh(MeshId.squareFloor, Vector3(0f, 0f, upper.height)),
              placeCubeRoomWalls(MeshId.squareWall)
          )
      )
//          +
//          cubeWallLamps(lampRate = 0.5f, heightOffset = upper.height - 0.1f)
      ,

      newSlope(lower, upper) +
          BlockBuilder(block = Block(name = "halfStepSlopeA$levelIndex")) +
          plainSlopeSlot(lower.height)
      ,

      newLedgeSlope(lower, upper, "halfStepSlopeAndLedgeA$levelIndex", -1),
      newLedgeSlope(lower, upper, "halfStepSlopeAndLedgeB$levelIndex", 1)

//      "diagonalCorner$levelIndex" to diagonalCornerFloor(upper.height) + BlockBuilder(
//          block = Block(
//              sides = sides(
//                  up = upper.up,
//                  east = upper.side,
//                  north = upper.side
//              )
//          )
//      )
  )
}

fun heights(): List<BlockBuilder> =
    (1..3)
        .map(::explodeHeightBlocks)
        .reduce { a, b -> a.plus(b) }
        .plus(
            newSlope(levels.last(), Level(0, endpoint, Sides.verticalDiagonal)) +
                BlockBuilder(block = Block(name = "levelWrap"))
        )
