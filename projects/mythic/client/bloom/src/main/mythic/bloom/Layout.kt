package mythic.bloom

import org.joml.Vector2i
import org.joml.plus

typealias FlexBounds = List<Int?> // Size = 6

class FlexProperty {
  companion object {

    val left = 0
    val top = 1
    val right = 2
    val bottom = 3
    val width = 4
    val height = 5
  }
}

typealias BloomId = Int

typealias FlexPair = Pair<FlexBounds, List<FlexBounds>>
typealias FlexTriple = Triple<FlexBounds, FlexBounds, List<FlexBounds>>
typealias Arranger = (FlexTriple) -> FlexPair

typealias ArrangerMap = Map<BloomId, Arranger>
typealias IdMap = Map<BloomId, BloomId>
typealias FlexBoundsMap = Map<BloomId, FlexBounds>
typealias FlowerTransform = (Flower) -> Flower

val emptyFlexBounds = listOf(null, null, null, null, null, null)

fun mapBlossomBoxes(transform: (Boxes) -> Boxes): (Blossom) -> Blossom = { blossom ->
  blossom.copy(
      boxes = transform(blossom.boxes)
  )
}

fun newInitialFlexBounds(ids: Collection<BloomId>) =
    ids.map { Pair(it, emptyFlexBounds) }
        .associate { it }

fun arrange(parentMap: IdMap, arrangers: ArrangerMap, initialBounds: FlexBoundsMap): FlexBoundsMap {
  val childrenMap = arrangers.mapValues { (id, _) ->
    parentMap.filter { it.value == id }
        .map { it.key }
  }
  var bounds: FlexBoundsMap = initialBounds
  var lastRemaining = arrangers.size + 1

  while (true) {
    val remaining = bounds.filter { b -> b.value.any { it == null } }
        .map { it.key }

    if (remaining.none())
      break

    if (remaining.size == lastRemaining)
      throw Error("Possible infinite loop")

    lastRemaining = remaining.size

    for (id in remaining) {
      val arranger = arrangers[id]!!
      val parent = bounds[parentMap[id]] ?: emptyFlexBounds
      val childIds = childrenMap[id]!!
      val self = bounds[id]!!
      val children = childIds.map { bounds[it]!! }
      val (updatedSelf, updatedChildren) = arranger(Triple(parent, self, children))
      bounds = bounds
          .plus(Pair(id, updatedSelf))
          .plus(childIds.zip(updatedChildren))
    }
  }

  return bounds
//      .map { (id, it) ->
//    Pair(id, Bounds(it[0]!!, it[1]!!, it[4]!!, it[5]!!))
//  }.associate { it }
}

fun arrangeWithInitial(parentMap: IdMap, arrangers: ArrangerMap) =
    arrange(parentMap, arrangers, newInitialFlexBounds(arrangers.keys))

fun convertBounds(bounds: Bounds): FlexBounds =
    listOf(
        bounds.position.x,
        bounds.position.y,
        bounds.position.x + bounds.dimensions.x,
        bounds.position.y + bounds.dimensions.y,
        bounds.dimensions.x,
        bounds.dimensions.y
    )

fun resolveLengths(boundLength: Int, lengths: List<Int?>): List<Int> {
  val exacts = lengths.filterNotNull()
  val total = exacts.sum()

  if (exacts.size == lengths.size) {
    if (total != boundLength)
      throw Error("Could not stretch or shrink to fit bounds")

    return exacts
  } else {
    val stretchCount = lengths.size - exacts.size
    val stretchLength = (boundLength - total) / stretchCount
    return lengths.map { if (it != null) it else stretchLength }
  }
}

fun applyLengths(start: Int, lengths: List<Int>, planeOffset: Int, bounds: Collection<FlexBounds>): List<FlexBounds> {
  val offsets = lengths.fold(listOf(0)) { a, b -> a.plus(b + a.last()) }
  return bounds.mapIndexed { index, bound ->
    val offset = offsets[index]
    val length = lengths[index]
    val record = bound.toMutableList()
    record[0 + planeOffset] = offset
    record[2 + planeOffset] = offset + length
    record[4 + planeOffset] = length
    record.toList()
  }
}

val isResolved = { b: FlexBounds -> b.all { it != null } }

fun independentBoundsTransform(transform: (Bounds) -> Bounds): FlowerTransform = { flower ->
  { flower(Seed(it.bag, transform(it.bounds))) }
}

fun moveBounds(offset: Vector2i): (Bounds) -> Bounds = { bounds ->
  bounds.copy(
      position = bounds.position + offset
  )
}

fun dependentBoundsTransform(transform: (Vector2i, Vector2i) -> Vector2i): FlowerTransform = { flower ->
  { seed ->
    val result = flower(seed)
    val boxes = result.boxes
    if (boxes.none())
      result
    else {
      val offset = transform(seed.bounds.dimensions, boxes.first().bounds.dimensions)
      Blossom(
          boxes = boxes.map {
            it.copy(
                bounds = it.bounds.copy(
                    position = it.bounds.position + offset
                )
            )
          },
          bounds = moveBounds(offset)(result.bounds)
      )
    }
  }
}

fun offset(flower: Flower): (Vector2i) -> Flower = { value ->
  { flower(Seed(it.bag, Bounds(it.bounds.position + value, it.bounds.dimensions))) }
}

fun offset(value: Vector2i): FlowerTransform = independentBoundsTransform {
  Bounds(it.position + value, it.dimensions)
}

fun centeredHorizontalVertical(bounds: Bounds, contentDimensions: Vector2i): Vector2i =
    Vector2i(
        centeredPosition(horizontal, bounds.dimensions, contentDimensions.x),
        centeredPosition(vertical, bounds.dimensions, contentDimensions.y)
    )

fun moveBounds(mover: (Bounds, Vector2i) -> Vector2i): (Bounds, Vector2i) -> Bounds = { bounds, contentDimensions ->
  Bounds(
      mover(bounds, contentDimensions) + bounds.position,
      contentDimensions
  )
}

fun centeredHorizontalPosition(bounds: Bounds, contentDimensions: Vector2i): Vector2i {
  val dimensions = bounds.dimensions
  return bounds.position + Vector2i(
      centeredPosition(horizontal, dimensions, contentDimensions.x),
      dimensions.y
  )
}

val centeredHorizontal: FlowerTransform = dependentBoundsTransform { parent, child ->
  Vector2i(
      (parent.x - child.x) / 2,
      0
  )
}

val centeredVertical: FlowerTransform = dependentBoundsTransform { parent, child ->
  Vector2i(
      0,
      (parent.y - child.y) / 2
  )
}

val centeredBoth: FlowerTransform = dependentBoundsTransform { parent, child ->
  Vector2i(
      (parent.x - child.x) / 2,
      (parent.y - child.y) / 2
  )
}