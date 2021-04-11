package silentorb.mythic.performing

import silentorb.mythic.breeze.AnimationName
import silentorb.mythic.ent.Id
import simulation.characters.EquipmentSlot
import simulation.main.Deck
import simulation.misc.Definitions

data class ActionDefinition(
    val type: String = "legacy",
    val cooldown: Float = 1f,
    val equipmentSlot: EquipmentSlot,
    val range: Float = 0f,
    val animation: AnimationName? = null,
    val duration: Float = 0f
)

data class Action(
    val cooldown: Float = 0f
)

fun updateCooldown(definitions: Definitions, deck: Deck, activated: List<Id>, id: Id, action: Action, delta: Float): Float {
  val isActivated = activated.contains(id)
  val hasPerformance = deck.performances.any { it.value.sourceAction == id }
  val accessory = deck.accessories[id]!!
  val definition = definitions.actions[accessory.value.type]!!
  val cooldown = definition.cooldown

  return if (isActivated || hasPerformance)
    cooldown
  else if (action.cooldown > 0f)
    Math.max(0f, action.cooldown - delta)
  else
    0f
}
