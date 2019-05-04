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
typealias FlowerTransformOld = (FlowerOld) -> FlowerOld

val emptyFlexBounds = listOf(null, null, null, null, null, null)

fun mapBlossomBoxes(transform: (FlatBoxes) -> FlatBoxes): (Blossom) -> Blossom = { blossom ->
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

fun independentBoundsTransform(transform: (Bounds) -> Bounds): FlowerTransformOld = { flower ->
  { flower(SeedOld(it.bag, transform(it.bounds))) }
}

//fun clippedDimensions(parent: Bounds, childPosition: Vector2i, childDimensions: Vector2i): Vector2i {
//  return if (childPosition.x + childDimensions.x > parent.right ||
//      childPosition.y + childDimensions.y > parent.bottom)
//    Vector2i(
//        x = Math.min(childDimensions.x, parent.right - childPosition.x),
//        y = Math.min(childDimensions.y, parent.bottom - childPosition.y)
//    )
//  else
//    childDimensions
//}

fun clippedDimensions(parent: Vector2i, childPosition: Vector2i, childDimensions: Vector2i): Vector2i {
  return if (childPosition.x + childDimensions.x > parent.x ||
      childPosition.y + childDimensions.y > parent.y)
    Vector2i(
        x = Math.min(childDimensions.x, parent.x - childPosition.x),
        y = Math.min(childDimensions.y, parent.y - childPosition.y)
    )
  else
    childDimensions
}

//fun moveBoundsOld(reverseOffset: Vector2i, parent: Bounds): (Bounds) -> Bounds = { child ->
//  val newPosition = child.position + reverseOffset
//  val newDimensions = clippedDimensions(parent, newPosition, child.dimensions)
//
//  child.copy(
//      position = newPosition,
//      dimensions = newDimensions
//  )
//}

fun moveBounds(offset: Vector2i, container: Vector2i): (Bounds) -> Bounds = { child ->
  val newPosition = child.position + offset
  val newDimensions = clippedDimensions(container, newPosition, child.dimensions)

  child.copy(
      position = newPosition,
      dimensions = newDimensions
  )
}


//fun dependentBoundsTransformOld(transform: (Vector2i, Vector2i) -> Vector2i): FlowerTransformOld = { flower ->
//  { seed ->
//    val result = flower(seed)
//    val boxes = result.boxes
//    if (boxes.none())
//      result
//    else {
//      val reverseOffset = transform(seed.bounds.dimensions, boxes.first().bounds.dimensions)
//      Blossom(
//          boxes = boxes.map {
//            it.copy(
//                bounds = it.bounds.copy(
//                    position = it.bounds.position + reverseOffset
//                )
//            )
//          },
//          bounds = moveBoundsOld(reverseOffset, seed.bounds)(result.bounds)
//      )
//    }
//  }
//}

fun withOffset(flower: FlowerOld): (Vector2i) -> FlowerOld = { value ->
  { flower(SeedOld(it.bag, Bounds(it.bounds.position + value, it.bounds.dimensions))) }
}

fun withOffset(value: Vector2i): FlowerTransformOld = independentBoundsTransform {
  Bounds(it.position + value, it.dimensions)
}

typealias Positioner = (Vector2i) -> Int

typealias PlanePositioner = (PlaneMap) -> Positioner

typealias ReversePositioner = (Vector2i, Bounds, Bounds) -> Int

typealias ReversePlanePositioner = (PlaneMap) -> ReversePositioner

val centered: ReversePlanePositioner = { plane ->
  { parent, _, child ->
    (plane.x(parent) - plane.x(child.dimensions)) / 2
  }
}

fun fixedReverse(value: Int): ReversePlanePositioner = { plane ->
  { _, _, _ ->
    value
  }
}

fun percentage(value: Float): PlanePositioner = { plane ->
  { parent ->
    (plane.x(parent).toFloat() * value).toInt()
  }
}

fun FlowerTransformOld.plus(other: FlowerTransformOld): FlowerTransformOld = { flower ->
  val transition = this(flower)
  other(transition)
}

infix fun FlowerTransformOld.fork(a: FlowerOld): FlowerTransformOld = { b ->
  //  val transition = this(b)
  this { seed ->
    val blossom = b(seed)
    val newSeed = seed.copy(
        bounds = blossom.bounds
    )
    blossom.append(a(newSeed))
  }
}
