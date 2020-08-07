package generation.architecture.biomes

import generation.architecture.blocks.biomeAdapterCube
import generation.architecture.matrical.BlockBuilder
import simulation.misc.CellAttribute

fun biomeAdapters(): List<BlockBuilder> {
  val intermediary = listOf(
      BiomeId.checkers,
      BiomeId.forest
  )
  val edges = listOf(
      BiomeId.tealPalace,
      BiomeId.village
  )
  return listOf(
      BlockBuilder(biomeAdapterCube(BiomeId.forest, BiomeId.home, setOf(CellAttribute.unique))(0)!!, generalForestBuilder())
  )
//      .plus(
//          perHeights(0..3,
//              listOf(
//                  biomeAdapterCube(BiomeId.checkers, BiomeId.forest) to applyBuilderLevels(generalCheckersBuilder())
//              )
//          )
//      )
}
