package generation.architecture.blocks

import generation.architecture.biomes.forestBiome
import generation.architecture.definition.BiomeId
import generation.architecture.matrical.BlockBuilder
import generation.architecture.matrical.applyBiomeBlockBuilders

val commonMatrixBlocks = listOf(
    squareRoom,
    fullSlope,
    ledgeSlope,
    diagonalCorner
)

fun allBlockBuilders(): List<BlockBuilder> =
    homeBlocks()
        .plus(biomeAdapters())
        .plus(
            applyBiomeBlockBuilders(
                mapOf(
                    BiomeId.checkers to commonMatrixBlocks,
                    BiomeId.forest to forestBiome(),
                    BiomeId.tealPalace to commonMatrixBlocks,
                    BiomeId.village to commonMatrixBlocks
                )
            )
        )
