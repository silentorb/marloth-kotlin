package generation.architecture.biomes

import generation.architecture.blocks.biomeAdapterCube
import generation.architecture.matrical.BlockBuilder
import generation.architecture.matrical.applyBuilderLevels
import generation.architecture.matrical.perHeights
import simulation.misc.CellAttribute

fun biomeAdapters(): List<BlockBuilder> {
  val intermediary = listOf(
      Biomes.checkers,
      Biomes.forest
  )
  val edges = listOf(
      Biomes.tealPalace,
      Biomes.village
  )
  return listOf(
      BlockBuilder(biomeAdapterCube(Biomes.forest, Biomes.home, setOf(CellAttribute.unique))(0)!!, generalForestBuilder())
  )
      .plus(
          perHeights(0..3,
              listOf(
                  biomeAdapterCube(Biomes.checkers, Biomes.forest) to applyBuilderLevels(generalCheckersBuilder())
              )
          )
      )
}
