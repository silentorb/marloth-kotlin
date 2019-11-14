package generation.architecture.building

import generation.architecture.definition.ConnectionType
import generation.architecture.definition.any
import generation.elements.*
import generation.next.Builder
import randomly.Dice
import scenery.enums.MeshId
import simulation.misc.NodeAttribute

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

fun mergeAttributes(blocks: List<Block>): Set<NodeAttribute> =
    blocks.flatMap { it.attributes }.toSet()

fun compose(vararg blockBuilders: BlockBuilderElement): BlockBuilder {
  val blocks = blockBuilders.map { it.block }
  val builders = blockBuilders.mapNotNull { it.builder }
  return BlockBuilder(
      block = Block(
          sides = mergeSides(blocks),
          attributes = mergeAttributes(blocks)
      ),
      builder = { input ->
        builders.flatMap { it(input) }
      }
  )
}

data class BlockBuilderElement(
    val block: Block,
    val builder: Builder?
)

data class BlockBuilder(
    val block: Block,
    val builder: Builder
)

fun blockBuilder(up: Side = any,
                 down: Side = any,
                 east: Side = any,
                 north: Side = any,
                 west: Side = any,
                 south: Side = any,
                 attributes: Set<NodeAttribute> = setOf(),
                 builder: Builder? = null): BlockBuilderElement =
    BlockBuilderElement(
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

fun getSideMesh(dice: Dice, sides: Sides, direction: Direction, meshMap: Map<ConnectionType, MeshId>): MeshId? {
  val meshMap = mapOf(
      ConnectionType.wall to MeshId.squareWall,
      ConnectionType.doorway to MeshId.squareWallDoorway
  )
  val possibleMeshes = sides.getValue(direction).intersect(meshMap.keys)
  if (possibleMeshes.none())
    throw Error("No valid side found for wall mesh")

  return meshMap[dice.takeOne(possibleMeshes)]!!
}
