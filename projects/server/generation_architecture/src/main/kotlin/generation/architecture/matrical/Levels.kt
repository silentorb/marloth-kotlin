package generation.architecture.matrical

import generation.architecture.blocks.octaveDiagonalCorner
import generation.architecture.blocks.singleCellRoom
import generation.architecture.blocks.slopeSides
import generation.architecture.blocks.tieredBlocks
import generation.architecture.building.*
import generation.architecture.definition.*
import generation.general.*
import silentorb.mythic.spatial.Vector3
import simulation.misc.BiomeName
import simulation.misc.CellAttribute
import simulation.misc.cellLength

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

private val levels = listOf(
    newLevel(0, Connector.open, endpoint, setOf(levelLedgeConnectors[0])),
    newLevel(1, Connector.quarterLevelOpen1, endpoint, setOf(levelLedgeConnectors[1])),
    newLevel(2, Connector.quarterLevelOpen2, endpoint, setOf(levelLedgeConnectors[2])),
    newLevel(3, Connector.quarterLevelOpen3, Sides.extraHeadroom, setOf(levelLedgeConnectors[3]))
)

fun tieredWalls(level: Level) =
    cubeWallsWithFeatures(listOf(WallFeature.lamp, WallFeature.none), offset = Vector3(0f, 0f, level.height - 1.2f))

data class CommonMatrixSides(
    val halfStepRequiredOpen: Side,
    val halfStepOptionalOpen: Side
)

data class BlockMatrixInput(
    val level: Level,
    val biome: BiomeName,
    val sides: CommonMatrixSides
//    val secondaryBiomes: List<BiomeName> = listOf()
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

typealias MatrixBlockBuilder = (BlockMatrixInput) -> List<BiomedBlockBuilder>

fun getLowerLevelIndex(index: Int): Int =
    (index + levels.size - 1) % levels.size

fun getLowerLevel(level: Level): Level =
    levels[getLowerLevelIndex(level.index)]

data class BiomeConnector(
    val biome: BiomeName,
    val connector: Any
)

fun newBiomeSide(biome: BiomeName, side: Side): Side =
    side.copy(
        mine = BiomeConnector(biome, side.mine),
        other = side.other
            .map { BiomeConnector(biome, it) }
            .toSet()
    )

fun heights(biome: BiomeName): List<BlockBuilder> =
    (1..3)
        .asSequence()
        .map(newBlockMatrixInput(biome))
        .map(::tieredBlocks)
        .reduce { a, b -> a.plus(b) }
        .plus(
            listOf(
                singleCellRoom(),
                octaveDiagonalCorner(),
                BiomedBlockBuilder(
                    block = Block(
                        name = "levelWrap",
                        sides = slopeSides(levels.last(), Level(0, endpoint, Sides.verticalDiagonal)),
                        attributes = setOf(CellAttribute.traversable)
                    ),
                    builder = slopeBuilder(levels.last())
                )
            )
        )
        .map(applyBiomedBlockBuilder(biome))
        .toList()
