package generation.architecture.building

import generation.architecture.misc.Builder
import generation.architecture.misc.BuilderInput
import generation.general.*
import simulation.main.Hand
import simulation.misc.CellAttribute

fun mergeSides(blocks: List<Block>): Sides {
  val sides = allDirections.associateWith { direction ->
    val options = blocks
        .mapNotNull { block -> block.sides[direction] }
        .filter { it != endpoint }

//    assert(options.size < 2)
    options.lastOrNull() ?: endpoint
  }

  return sides
}

fun mergeBlocks(blocks: List<Block>): Block =
    Block(
        name = blocks.firstOrNull { it.name.isNotEmpty() }?.name ?: "",
        sides = mergeSides(blocks),
        attributes = blocks.flatMap { it.attributes }.toSet(),
        slots = blocks.flatMap { it.slots }
    )

fun mergeBuilders(vararg builders: Builder): Builder {
  return { input ->
    builders.flatMap { it(input) }
  }
}

fun compose(vararg blockBuilders: BlockBuilder): BlockBuilder {
  val blocks = blockBuilders.map { it.block }
  val builders = blockBuilders.mapNotNull { it.builder }
  return BlockBuilder(
      block = mergeBlocks(blocks),
      builder = { input ->
        builders.flatMap { it(input) }
      }
  )
}

fun compose(attributes: Set<CellAttribute>, vararg blockBuilders: BlockBuilder): BlockBuilder =
    compose(*(blockBuilders.toList().plus(BlockBuilder(block = Block(attributes = attributes))).toTypedArray()))

data class BlockBuilder(
    val block: Block,
    val builder: Builder? = null
) {
  operator fun plus(other: BlockBuilder) = compose(this, other)
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

fun blockBuilder(up: Side = endpoint,
                 down: Side = endpoint,
                 east: Side = endpoint,
                 north: Side = endpoint,
                 west: Side = endpoint,
                 south: Side = endpoint,
                 attributes: Set<CellAttribute> = setOf(),
                 builder: Builder? = null): BlockBuilder =
    BlockBuilder(
        block = Block(
            sides = mapOf(
                Direction.up to up,
                Direction.down to down,
                Direction.east to east,
                Direction.north to north,
                Direction.west to west,
                Direction.south to south
            ),
            attributes = attributes
        ),
        builder = builder
    )

//fun getSideMesh(dice: Dice, sides: Sides, direction: Direction, meshMap: Map<ConnectionType, Set<MeshId>>): MeshId? {
//  val possibleMeshes = sides.getValue(direction).intersect(meshMap.keys)
//  if (possibleMeshes.none())
//    return null
//
//  return dice.takeOne(meshMap[dice.takeOne(possibleMeshes)]!!)
//}

typealias BlockBuilderTransform = (BlockBuilder) -> BlockBuilder

fun wrapBlockBuilder(addition: (BuilderInput, List<Hand>) -> List<Hand>): BlockBuilderTransform = { blockBuilder ->
  blockBuilder.copy(
      builder = { input ->
        val subBuilder = blockBuilder.builder
        val hands = if (subBuilder != null)
          subBuilder(input)
        else
          listOf()

        hands.plus(addition(input, hands))
      }
  )
}

fun withCellAttributes(attributes: Set<CellAttribute>): (BlockBuilder) -> BlockBuilder = { blockBuilder ->
  val block = blockBuilder.block
  blockBuilder.copy(
      block = block.copy(
          attributes = block.attributes.plus(attributes)
      )
  )
}
