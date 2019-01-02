package mythic.bloom

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
typealias BoundsMap = Map<BloomId, Bounds>

//fun newInitialFlexBounds(ids: Collection<BloomId>, rootId: BloomId, rootBounds: FlexBounds) =
//    ids.map { Pair(it, listOf(null, null, null, null, null, null)) }
//        .associate { it }
//        .plus(Pair(rootId, rootBounds))

val emptyBounds = listOf(null, null, null, null, null, null)

fun newInitialFlexBounds(ids: Collection<BloomId>) =
    ids.map { Pair(it, emptyBounds) }
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
      val parent = bounds[parentMap[id]] ?: emptyBounds
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