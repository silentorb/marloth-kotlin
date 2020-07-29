package generation.architecture.matrical

import generation.general.Side
import simulation.misc.BiomeName
import simulation.misc.cellLength

const val quarterStep = cellLength / 4f

fun getLevelHeight(level: Int): Float = level.toFloat() * quarterStep

const val levelCount = 4

data class BlockMatrixInput(
    val level: Int,
    val biome: BiomeName
)

fun newBlockMatrixInput(biome: BiomeName): (Int) -> BlockMatrixInput = { level ->
  BlockMatrixInput(
      level = level,
      biome = biome
  )
}

typealias MatrixBlockBuilder = (BlockMatrixInput) -> List<BiomedBlockBuilder>

typealias Blueprint = List<MatrixBlockBuilder>

fun getLowerLevelIndex(index: Int): Int =
    (index + levelCount - 1) % levelCount

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

fun tieredBlocks(blockBuilders: List<MatrixBlockBuilder>): (BlockMatrixInput) -> List<BiomedBlockBuilder> = { input ->
  blockBuilders
      .flatMap {
        it(input)
      }
}

fun perHeights(biome: BiomeName, blockBuilders: List<MatrixBlockBuilder>): List<BlockBuilder> =
    (0..3)
        .map(newBlockMatrixInput(biome))
        .flatMap(tieredBlocks(blockBuilders))
        .map(applyBiomedBlockBuilder(biome))

fun applyBiomeBlockBuilders(biomeBlockBuilders: Map<BiomeName, List<MatrixBlockBuilder>>): List<BlockBuilder> =
    biomeBlockBuilders
        .flatMap { (biome, blockBuilders) ->
          perHeights(biome, blockBuilders)
        }
