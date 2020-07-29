package generation.architecture.blocks

import generation.architecture.definition.BiomeId
import generation.architecture.matrical.*
import silentorb.mythic.spatial.Vector3
import simulation.misc.BiomeName

fun plainWallLampOffset() = Vector3(0f, 0f, -1f)

val commonMatrixBlocks = listOf(
    squareRoom,
    fullSlope,
    ledgeSlope,
    diagonalCorner
)

fun tieredBlocks(input: BlockMatrixInput): List<BiomedBlockBuilder> =
    commonMatrixBlocks
        .flatMap {
          it(input)
        }

fun heights(biome: BiomeName): List<BlockBuilder> =
    (0..3)
        .map(newBlockMatrixInput(biome))
        .flatMap(::tieredBlocks)
        .plus(
            slopeWrapper()
        )
        .map(applyBiomedBlockBuilder(biome))

fun allBlockBuilders(): List<BlockBuilder> =
    homeBlocks()
        .plus(biomeAdapters())
        .plus(
            listOf(
                BiomeId.checkers,
                BiomeId.forest,
                BiomeId.tealPalace,
                BiomeId.village
            )
                .flatMap(::heights)
        )
