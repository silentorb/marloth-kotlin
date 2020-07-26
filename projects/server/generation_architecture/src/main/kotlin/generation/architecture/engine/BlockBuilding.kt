package generation.architecture.engine

import generation.architecture.misc.Builder
import generation.general.Block
import generation.general.Direction
import generation.general.Side
import generation.general.endpoint
import simulation.main.Hand

fun mergeBuilders(vararg builders: Builder): Builder {
  return { input ->
    builders.flatMap { it(input) }
  }
}

fun handBuilder(hand: Hand): Builder = { input ->
  listOf(hand)
}

data class BlockBuilder(
    val block: Block,
    val builder: Builder? = null
)

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
