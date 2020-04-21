package generation.architecture.building

import generation.architecture.misc.Builder
import generation.architecture.definition.ConnectionType
import generation.architecture.definition.any
import generation.architecture.misc.BuilderInput
import generation.general.*
import silentorb.mythic.randomly.Dice
import marloth.scenery.enums.MeshId
import silentorb.mythic.spatial.Vector3
import simulation.main.Hand
import simulation.misc.CellAttribute

fun mergeSides(blocks: List<Block>): Sides {
  val sides = allDirections.associateWith { direction ->
    blocks
        .mapNotNull { block -> block.sides[direction] }
        .flatten()
        .toSet()
  }

  assert(sides.all { it.value.any() })
  return sides
}

fun mergeBlocks(blocks: List<Block>): Block =
    Block(
        sides = mergeSides(blocks),
        attributes = blocks.flatMap { it.attributes }.toSet(),
        slots = blocks.flatMap { it.slots }
    )

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
    up: Side = any,
    down: Side = any,
    east: Side = any,
    north: Side = any,
    west: Side = any,
    south: Side = any
) = mapOf(
    Direction.up to up,
    Direction.down to down,
    Direction.east to east,
    Direction.north to north,
    Direction.west to west,
    Direction.south to south
)

fun blockBuilder(up: Side = any,
                 down: Side = any,
                 east: Side = any,
                 north: Side = any,
                 west: Side = any,
                 south: Side = any,
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

fun getSideMesh(dice: Dice, sides: Sides, direction: Direction, meshMap: Map<ConnectionType, Set<MeshId>>): MeshId? {
  val possibleMeshes = sides.getValue(direction).intersect(meshMap.keys)
  if (possibleMeshes.none())
    return null

  return dice.takeOne(meshMap[dice.takeOne(possibleMeshes)]!!)
}

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