package generation.architecture.matrical

import generation.architecture.building.WallFeature
import generation.architecture.building.cubeWallsWithFeatures
import generation.general.Side
import silentorb.mythic.spatial.Vector3
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
