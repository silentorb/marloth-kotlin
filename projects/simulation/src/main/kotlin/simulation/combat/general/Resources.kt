package simulation.combat.general

import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events

typealias ResourceTypeName = String

typealias ResourceMap = Map<ResourceTypeName, Int>

val emptyResourceMap: ResourceMap = mapOf()

object ResourceTypes {
  val health = "health"
  val energy = "energy"
  val sanity = "sanity"
  val money = "money"
}

//fun getResources(graph: AnyGraph): List<Any> =
//    filterByAttribute(graph, GameAttributes.resource)

enum class ResourceOperation {
  add,
  replace,
}

data class ModifyResource(
    val actor: Id,
    val resource: String,
    val amount: Int,
    val operation: ResourceOperation = ResourceOperation.add,
)

data class ResourceContainer(
    val value: Int,
    val max: Int = value
)

data class ResourceBundle(
    val values: ResourceMap,
    val maximums: ResourceMap = emptyResourceMap
)

fun clampResource(value: Int, max: Int): Int {
  return when {
    value < 0 -> 0
    value > max -> max
    else -> value
  }
}

fun modifyResource(value: Int, max: Int, mod: Int): Int =
    clampResource(mod + value, max)

fun modifyResourceWithEvents(events: Events, actor: Id, resource: String, previous: Int, max: Int, mod: Int): Int {
  val modifyEvents =
      events
          .filterIsInstance<ModifyResource>()

  val (replacements, additions) = modifyEvents
      .filter { it.actor == actor && it.resource == resource }
      .partition { it.operation == ResourceOperation.replace }

  val replacement = replacements.maxOfOrNull { it.amount }
  val base = replacement ?: previous
  val raw = base + additions.sumBy { it.amount } + mod

  return clampResource(raw, max)
}
