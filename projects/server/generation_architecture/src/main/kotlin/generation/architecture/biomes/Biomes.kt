package generation.architecture.biomes

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
