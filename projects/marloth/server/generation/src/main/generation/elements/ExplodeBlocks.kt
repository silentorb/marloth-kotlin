package generation.elements

data class MappedBlock(
    val original: Block,
    val turns: Int = 0
)

typealias BlockMap = Map<Block, MappedBlock>

fun explodeBlockMap(blocks: Set<Block>): BlockMap {
  val nonFree = blocks.filter { it.sides != rotateSides(1)(it.sides) }
  val rotated = nonFree.flatMap { originalBlock ->
    (1..3)
        .map { turns ->
          val block = originalBlock.copy(
              sides = rotateSides(turns)(originalBlock.sides)
          )
          Pair(block, MappedBlock(original = originalBlock, turns = turns))
        }
  }

  return blocks
      .associateWith { MappedBlock(original = it) }
      .plus(rotated)
}
