package mythic.ent

private val cacheMap: MutableMap<Int, Pair<Int, Any>> = mutableMapOf()

fun <I, O> functionCache(func: (I) -> O): (I) -> O {
  var entry: Pair<Int, O>? = null

  return { input ->
    val localEntry = entry
    if (localEntry != null && localEntry.first == input.hashCode() && localEntry.second != null) {
      localEntry.second
    } else {
      val key = input.hashCode()
      val output = func(input)
      entry = Pair(key, output)
      output
    }
  }
}