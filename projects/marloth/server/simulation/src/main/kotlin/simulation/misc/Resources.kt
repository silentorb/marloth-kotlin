package simulation.misc

import scenery.enums.ResourceId

typealias ResourceMap = Map<ResourceId, Int>

val emptyResourceMap: ResourceMap = mapOf()

data class ResourceContainer(
    val value: Int,
    val max: Int = value
)

data class ResourceBundle(
    val values: ResourceMap,
    val maximums: ResourceMap = emptyResourceMap
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

fun modifyResource(resource: ResourceContainer, mod: Int): Int =
    modifyResource(resource.value, resource.max, mod)
