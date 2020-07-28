package generation.architecture.blocks

import generation.architecture.definition.BiomeId
import generation.architecture.definition.levelSides
import generation.architecture.engine.squareOffsets
import generation.architecture.matrical.BlockBuilder
import generation.architecture.matrical.applyBiomedBuilder
import generation.architecture.matrical.newBiomeSide
import generation.architecture.matrical.sides
import generation.general.Block
import simulation.misc.BiomeName
import simulation.misc.CellAttribute

fun biomeAdapterCube(firstBiome: BiomeName, secondBiome: BiomeName, attributes: Set<CellAttribute> = setOf()): (Int) -> BlockBuilder = { level ->
  val localSides = levelSides[level]
  val firstSide = newBiomeSide(firstBiome, localSides.open)
  val secondSide = newBiomeSide(secondBiome, localSides.open)

  BlockBuilder(
      block = Block(
          name = "$firstBiome-$secondBiome-adapterCube$level",
          sides = sides(
              east = firstSide,
              north = secondSide,
              west = firstSide,
              south = firstSide
          ),
          attributes = setOf(CellAttribute.traversable) + attributes,
          slots = squareOffsets(2)
      ),
      builder = applyBiomedBuilder(firstBiome, singleCellRoomBuilder(level))
  )
}

fun biomeAdapterHall(firstBiome: BiomeName, secondBiome: BiomeName, attributes: Set<CellAttribute> = setOf()): (Int) -> BlockBuilder = { level ->
  val localSides = levelSides[level]
  val firstSide = newBiomeSide(firstBiome, localSides.open)
  val secondSide = newBiomeSide(secondBiome, localSides.open)

  BlockBuilder(
      block = Block(
          name = "$firstBiome-$secondBiome-adapterHall$level",
          sides = sides(
              north = secondSide,
              south = firstSide
          ),
          attributes = setOf(CellAttribute.traversable) + attributes,
          slots = squareOffsets(2)
      ),
      builder = applyBiomedBuilder(firstBiome, singleCellRoomBuilder(level))
  )
}

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
      biomeAdapterCube(BiomeId.checkers, BiomeId.home, setOf(CellAttribute.unique))(0)
  )
      .plus(
          listOf(
              biomeAdapterCube(BiomeId.forest, BiomeId.home, setOf(CellAttribute.unique)),
              biomeAdapterCube(BiomeId.checkers, BiomeId.forest)
          )
              .plus(
                  intermediary.flatMap { biome ->
                    edges.map { biomeAdapterHall(biome, it) }
                  }
              )
              .flatMap { (0..2 step 2).map { level -> it(level) } }
      )

}
