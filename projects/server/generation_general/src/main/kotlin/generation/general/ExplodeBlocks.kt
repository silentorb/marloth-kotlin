package generation.general

import simulation.misc.CellAttribute

data class MappedBlock(
    val original: Block,
    val turns: Int = 0
)

//typealias BlockMap = Map<Block, MappedBlock>
typealias RotatingConnections = Map<Any, List<Any>>

//fun <T> rotateVerticalSides(rotatingConnections: RotatingConnections, turns: Int): (Map.Entry<T, Side>) -> Side = { side ->
//  side.value.map { connectionType ->
//    val rotating = rotatingConnections[connectionType]
//    if (rotating != null)
//      rotating[turns]
//    else
//      connectionType
//  }.toSet()
//}

fun rotateSides(turns: Int): (Sides) -> Sides = { sides ->
  val horizontal = horizontalDirectionList.map { sides[it]!! }
  val normalizedTurns = turns % 4

  val spunSides = horizontal
      .takeLast(normalizedTurns)
      .plus(horizontal.take(4 - normalizedTurns))

  val horizontalRotatedSides = horizontalDirectionList.zip(spunSides) { a, b -> Pair(a, b) }.associate { it }

  val verticalRotatedSides = sides
      .filterKeys { verticalSides.contains(it) }
//      .mapValues(rotateVerticalSides(rotatingConnections, turns))

  verticalRotatedSides
      .plus(horizontalRotatedSides)
}

//fun explodeBlockMap(blocks: Set<Block>): Set<Block> {
//  val needsRotatedVariations = blocks.filter {
//    !it.attributes.contains(CellAttribute.lockedRotation) &&
//        it.sides != rotateSides(1)(it.sides)
//  }
//  val rotated = needsRotatedVariations.flatMap { originalBlock ->
//    (1..3)
//        .map { turns ->
//         originalBlock.copy(
//              sides = rotateSides(turns)(originalBlock.sides)
//          )
//        }
//  }
//
//  return blocks + rotated
//}
