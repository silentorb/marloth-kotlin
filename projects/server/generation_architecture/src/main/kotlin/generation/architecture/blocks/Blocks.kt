package generation.architecture.blocks

import generation.architecture.biomes.checkersBiome
import generation.architecture.biomes.forestBiome
import generation.architecture.biomes.homeBlocks
import generation.architecture.definition.BiomeId
import generation.architecture.matrical.BlockBuilder
import generation.architecture.matrical.applyBiomeBlockBuilders

//val commonMatrixBlocks = listOf(
//    squareRoomOld,
//    fullSlope,
//    ledgeSlope,
//    diagonalCorner
//)

fun allBlockBuilders(): List<BlockBuilder> =
    homeBlocks()
        .plus(biomeAdapters())
        .plus(
            applyBiomeBlockBuilders(
                mapOf(
                    BiomeId.checkers to checkersBiome(),
                    BiomeId.forest to forestBiome()
//                    BiomeId.tealPalace to commonMatrixBlocks,
//                    BiomeId.village to commonMatrixBlocks
                )
            )
        )
