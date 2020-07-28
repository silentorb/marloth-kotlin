package generation.architecture.matrical

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

fun getLevelHeight(level: Int): Float = level.toFloat() * quarterStep

data class Level(
    val index: Int,
    val side: Side,
    val up: Side = endpoint
) {
  val height: Float = getLevelHeight(index)
}

fun newLevel(index: Int): Level =
    Level(index, levelSides[index].open)

private val levels = (0..3).map { newLevel(it) }

fun tieredWalls(level: Int) =
    cubeWallsWithFeatures(listOf(WallFeature.lamp, WallFeature.none), lampOffset = Vector3(0f, 0f, getLevelHeight(level) - 1.2f))

data class CommonMatrixSides(
    val halfStepRequiredOpen: Side,
    val halfStepOptionalOpen: Side
)

data class BlockMatrixInput(
    val level: Int,
    val levelOld: Level,
    val biome: BiomeName,
    val sides: CommonMatrixSides
)

fun newBlockMatrixInput(biome: BiomeName): (Int) -> BlockMatrixInput = { levelIndex ->
  val upper = levels[levelIndex]
  BlockMatrixInput(
      level = levelIndex,
      levelOld = upper,
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
    (0..3)
        .map(newBlockMatrixInput(biome))
        .flatMap(::tieredBlocks)
        .plus(
            listOf(
                BiomedBlockBuilder(
                    block = Block(
                        name = "levelWrap",
                        sides = slopeSides(levels.last(), Level(0, Sides.slopeOctaveWrap, Sides.slopeOctaveWrap)),
                        attributes = setOf(CellAttribute.traversable)
                    ),
                    builder = slopeBuilder(levels.last())
                )
            )
        )
        .map(applyBiomedBlockBuilder(biome))
