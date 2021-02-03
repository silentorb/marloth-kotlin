package generation.architecture.matrical

import generation.architecture.engine.Builder
import generation.general.*
import simulation.main.Hand
import simulation.misc.BiomeName

typealias BlockBuilder = Pair<Block, Builder>

fun mergeBuilders(vararg builders: Builder): Builder {
  return { input ->
    builders.flatMap { it(input) as List<Hand> }
  }
}

operator fun Builder.plus(builder: Builder): Builder =
    mergeBuilders(this, builder)

fun handBuilder(hand: Hand): Builder = { input ->
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

fun restrictBiomeBlockSides(biome: BiomeName, block: Block): Block =
    block.copy(
        name = biome + "-" + block.name,
        sidesOld = block.sidesOld.mapValues { (_, side) ->
          if (side.isUniversal)
            side
          else
            newBiomeSide(biome, side)
        }
    )

fun applyBiomedBlockBuilder(biome: BiomeName): (BlockBuilder) -> BlockBuilder = { blockBuilder ->
  restrictBiomeBlockSides(biome, blockBuilder.first) to blockBuilder.second
}
