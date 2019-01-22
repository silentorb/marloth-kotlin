package mythic.ent

fun <T, N> firstSortedBy(comparison: (N, N) -> Boolean): (Collection<T>, (T) -> N) -> T = { list, accessor ->
  var result = list.first()
  var value = accessor(result)
  for (item in list.asSequence().drop(1)) {
    val nextValue = accessor(item)
    if (comparison(nextValue, value)) {
      result = item
      value = nextValue
    }
  }
  result
}

fun <T> Collection<T>.firstSortedBy(accessor: (T) -> Float): T =
    firstSortedBy<T, Float> { a, b -> a < b }(this, accessor)

fun <T, B> Collection<T>.firstNotNull(mapper: (T) -> B?): B? {
  for (item in this) {
    val result = mapper(item)
    if (result != null)
      return result
  }
  return null
}

fun <T> replace(collection: Collection<T>, newItem: T, matches: (T, T) -> Boolean) =
    collection.map {
      if (matches(it, newItem))
        newItem
      else
        it
    }

fun <T> transformNotNull(value: T?, action: (T) -> T): T? =
    if (value != null)
      action(value)
    else
      null

fun <T> pass(value: T): T = value
