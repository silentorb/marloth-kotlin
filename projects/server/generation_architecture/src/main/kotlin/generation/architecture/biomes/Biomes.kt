package generation.architecture.biomes

import generation.architecture.blocks.headroomBlock
import generation.architecture.building.emptyBuilder
import generation.architecture.matrical.BlockBuilder
import generation.architecture.matrical.applyBiomeBlockBuilders

object BiomeId {
  val checkers = "checkers"
  val forest = "forest"
  val home = "home"
  val tealPalace = "tealPalace"
  val village = "village"
}

//val commonMatrixBlocks = listOf(
//    squareRoomOld,
//    fullSlope,
//    ledgeSlope,
//    diagonalCorner
//)

fun allBlockBuilders(): List<BlockBuilder> =
    homeBlocks()
//        .plus(headroomBlock to emptyBuilder)
//        .plus(biomeAdapters())
//        .plus(
//            applyBiomeBlockBuilders(
//                mapOf(
//                    BiomeId.checkers to checkersBiome(),
//                    BiomeId.forest to forestBiome()
////                    BiomeId.tealPalace to commonMatrixBlocks,
////                    BiomeId.village to commonMatrixBlocks
//                )
//            )
//        )
