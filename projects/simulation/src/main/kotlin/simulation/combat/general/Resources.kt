package simulation.combat.general

typealias ResourceType = String

typealias ResourceMap = Map<ResourceType, Int>

val emptyResourceMap: ResourceMap = mapOf()

object ResourceTypes {
  val injury = "injury"
  val energy = "energy"
  val sanity = "sanity"
  val money = "money"
}

//fun getResources(graph: AnyGraph): List<Any> =
//    filterByAttribute(graph, GameAttributes.resource)

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
  return when {
    newValue < 0 -> 0
    newValue > max -> max
    else -> newValue
  }
}

fun modifyResource(resource: ResourceContainer, mod: Int): Int =
    modifyResource(resource.value, resource.max, mod)