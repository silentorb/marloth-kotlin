package generation.architecture.matrical

import generation.architecture.engine.ArchitectureInput
import generation.architecture.engine.Builder
import generation.general.*
import simulation.main.Hand
import simulation.misc.BiomeName

data class BiomedBuilderInput(
    val general: ArchitectureInput,
    val neighbors: Set<Direction>,
    val biome: BiomeInfo
)

typealias BiomedBuilder = (BiomedBuilderInput) -> List<Hand>

data class BlockBuilder(
    val block: Block,
    val builder: Builder? = null
)

data class BiomedBlockBuilder(
    val block: Block,
    val builder: BiomedBuilder
)

fun mergeBuilders(vararg builders: BiomedBuilder): BiomedBuilder {
  return { input ->
    builders.flatMap { it(input) }
  }
}

fun handBuilder(hand: Hand): BiomedBuilder = { input ->
  listOf(hand)
}

fun sides(
    up: Side = endpoint,
    down: Side = endpoint,
    east: Side = endpoint,
    north: Side = endpoint,
    west: Side = endpoint,
    south: Side = endpoint
) = mapOf(
    Direction.up to up,
    Direction.down to down,
    Direction.east to east,
    Direction.north to north,
    Direction.west to west,
    Direction.south to south
)

fun applyBiomedBuilder(biome: BiomeName, builder: BiomedBuilder): Builder =
    { input ->
      builder(BiomedBuilderInput(
          general = input.general,
          neighbors = input.neighbors,
          biome = input.general.config.biomes[biome]!!
      ))
    }

fun restrictBiomeBlockSides(biome: BiomeName, block: Block): Block =
    block.copy(
        name = biome + block.name,
        sides = block.sides.mapValues { (_, side) ->
          if (side == endpoint)
            side
          else
            newBiomeSide(biome, side)
        }
    )

fun applyBiomedBlockBuilder(biome: BiomeName): (BiomedBlockBuilder) -> BlockBuilder = { blockBuilder ->
  BlockBuilder(
      block = restrictBiomeBlockSides(biome, blockBuilder.block),
      builder = applyBiomedBuilder(biome, blockBuilder.builder)
  )
}
