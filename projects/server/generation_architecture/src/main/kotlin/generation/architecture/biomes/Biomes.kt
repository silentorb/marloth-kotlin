package generation.architecture.biomes

import generation.architecture.matrical.BlockBuilder
import generation.architecture.matrical.applyBiomeBlockBuilders

object BiomeId {
  const val checkers = "checkers"
  const val exit = "exit"
  const val forest = "forest"
  const val home = "home"
  const val tealPalace = "tealPalace"
  const val village = "village"
}

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
