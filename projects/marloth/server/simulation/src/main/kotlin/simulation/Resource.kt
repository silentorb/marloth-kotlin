package simulation

data class Resource(
    val value: Int,
    val max: Int = value
)

fun modifyResource(value: Int, max: Int, mod: Int): Int {
  val newValue = mod + value
  return if (newValue < 0)
    0
  else if (newValue > max)
    max
  else
    newValue
}

fun modifyResource(resource: Resource, mods: List<Int>): Int =
    mods.fold(resource.value, { a, mod -> modifyResource(a, resource.max, mod) })