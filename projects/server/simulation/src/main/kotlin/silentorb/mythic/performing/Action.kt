package silentorb.mythic.performing

import silentorb.mythic.breeze.AnimationName
import silentorb.mythic.ent.Id
import simulation.characters.EquipmentSlot
import simulation.main.Deck
import simulation.misc.Definitions

data class ActionDefinition(
    val cooldown: Float,
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
  val hasModifier = deck.modifiers.any { it.value.source == id }
  val accessory = deck.accessories[id]!!
  val definition = definitions.actions[accessory.type]!!
  val cooldown = definition.cooldown

  return if (isActivated || hasPerformance || hasModifier)
    cooldown
  else if (action.cooldown > 0f)
    Math.max(0f, action.cooldown - delta)
  else
    0f
}
