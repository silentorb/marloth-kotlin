package generation.architecture.building

import generation.architecture.definition.*
import generation.architecture.misc.squareOffsets
import generation.architecture.old.getTurnDirection
import generation.general.*
import marloth.scenery.enums.MeshId
import silentorb.mythic.spatial.Vector3
import simulation.misc.BiomeName
import simulation.misc.CellAttribute
import simulation.misc.cellLength
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
    newLevel(1, Connector.quarterLevelOpen1, endpoint, setOf(levelLedgeConnectors[1])),
    newLevel(2, Connector.quarterLevelOpen2, endpoint, setOf(levelLedgeConnectors[2])),
    newLevel(3, Connector.quarterLevelOpen3, Sides.extraHeadroom, setOf(levelLedgeConnectors[3]))
)

fun slopeSides(lower: Level, upper: Level) =
    sides(
        up = upper.up,
        east = upper.side,
        west = lower.side,
        north = newSlopeSide(lower.index, true),
        south = newSlopeSide(lower.index, false)
    )

fun tieredWalls(level: Level) =
    cubeWallsWithFeatures(listOf(WallFeature.lamp, WallFeature.none), offset = Vector3(0f, 0f, level.height - 1.2f))

fun newSlope(lower: Level, upper: Level) =
    BlockBuilder(
        block = Block(
            sides = slopeSides(lower, upper),
            attributes = setOf(CellAttribute.traversable)
        ),
        builder = mergeBuilders(
            newSlopedFloorMesh(MeshId.quarterSlope, lower.height),
            tieredWalls(lower)
        )
    )

fun plainSlopeSlot(lowerHeight: Float) =
    BlockBuilder(
        block = Block(
            slots = listOf(Vector3(0f, 0f, lowerHeight + quarterStep / 2f + 0.05f) + floorOffset)
        )
    )

fun newLedgeSide(level: Int) =
    newSide(levelLedgeConnectors[level], setOf(levelConnectors[level]), ConnectionLogic.connectWhenPossible)

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
          tieredWalls(lower)
      )
  )
}

data class CommonMatrixSides(
    val halfStepRequiredOpen: Side,
    val halfStepOptionalOpen: Side
)

data class BlockMatrixInput(
    val level: Level,
    val biome: BiomeName,
    val sides: CommonMatrixSides,
    val secondaryBiomes: List<BiomeName> = listOf()
)

fun newBlockMatrixInput(biome: BiomeName): (Int) -> BlockMatrixInput = { levelIndex ->
  val upper = levels[levelIndex]
  BlockMatrixInput(
      level = upper,
      biome = biome,
      sides = CommonMatrixSides(
          halfStepRequiredOpen = upper.side,
          halfStepOptionalOpen = upper.side
      )
  )
}

typealias MatrixBlockBuilder = (BlockMatrixInput) -> List<BlockBuilder>

fun getLowerLevelIndex(index: Int): Int =
    (index + levels.size - 1) % levels.size

fun getLowerLevel(level: Level): Level =
    levels[getLowerLevelIndex(level.index)]

fun tieredSquareFloorBuilder(upper: Level) = mergeBuilders(
    floorMesh(MeshId.squareFloor, Vector3(0f, 0f, upper.height)),
    tieredWalls(getLowerLevel(upper))
)

val squareRoom: MatrixBlockBuilder = { input ->
  val level = input.level
  val levelIndex = level.index
  val halfStepOptionalOpen = input.sides.halfStepOptionalOpen
  listOf(
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
              slots = squareOffsets(2).map { it + Vector3(0f, 0f, level.height) }
          ),
          builder = tieredSquareFloorBuilder(level)
      )
  )
}

val fullSlope: MatrixBlockBuilder = { input ->
  val upper = input.level
  val lower = getLowerLevel(upper)
  val levelIndex = upper.index
  listOf(
      newSlope(lower, upper) +
          BlockBuilder(block = Block(name = "halfStepSlopeA$levelIndex")) +
          plainSlopeSlot(lower.height)
  )
}

val ledgeSlope: MatrixBlockBuilder = { input ->
  val upper = input.level
  val lower = getLowerLevel(upper)
  val levelIndex = upper.index
  listOf(
      newLedgeSlope(lower, upper, "LedgeSlopeA$levelIndex", -1),
      newLedgeSlope(lower, upper, "LedgeSlopeB$levelIndex", 1)
  )
}

val diagnoseCorner: MatrixBlockBuilder = { input ->
  val upper = input.level
  val levelIndex = upper.index
  val halfStepOptionalOpen = input.sides.halfStepOptionalOpen
  listOf(
      diagonalCorner(
          "diagonalCorner$levelIndex",
          upper.height,
          sides(
              up = Sides.extraHeadroom,
              east = halfStepOptionalOpen,
              north = halfStepOptionalOpen,
              west = preferredHorizontalClosed(levelConnectors[levelIndex]),
              south = preferredHorizontalClosed(levelConnectors[levelIndex])
          ),
          tieredSquareFloorBuilder(upper)
      )
  )
}

fun tieredBlocks(input: BlockMatrixInput): List<BlockBuilder> {
  return listOf(
      squareRoom,
      fullSlope,
      ledgeSlope,
      diagnoseCorner
  ).flatMap { it(input) }
}

fun heights(biome: BiomeName): List<BlockBuilder> =
    (1..3)
        .map(newBlockMatrixInput(biome))
        .map(::tieredBlocks)
        .reduce { a, b -> a.plus(b) }
        .plus(
            newSlope(levels.last(), Level(0, endpoint, Sides.verticalDiagonal)) +
                BlockBuilder(block = Block(name = "levelWrap"))
        )
