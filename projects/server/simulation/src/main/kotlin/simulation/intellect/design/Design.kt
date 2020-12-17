package simulation.intellect.design

import silentorb.mythic.ent.Id
import simulation.intellect.Pursuit
import simulation.intellect.Spirit
import simulation.intellect.SpiritAttributes
import simulation.intellect.assessment.Knowledge
import simulation.intellect.assessment.getVisibleEnemies
import simulation.main.Deck
import simulation.main.World
import simulation.misc.Definitions

fun updateTargetEnemy(world: World, character: Id, knowledge: Knowledge, pursuit: Pursuit): Id? {
  val visibleEnemies = getVisibleEnemies(world.deck.characters[character]!!, knowledge)
  return if (pursuit.targetEnemy != null && visibleEnemies.any { it.id == pursuit.targetEnemy })
    pursuit.targetEnemy
  else if (visibleEnemies.any())
    visibleEnemies.first().id
  else
    null
}

fun getActionRange(deck: Deck, definitions: Definitions): (Id) -> Float = { action ->
  val accessory = deck.accessories[action]!!
  val definition = definitions.actions[accessory.value.type]!!
  definition.range
}

fun updateAggression(world: World, actor: Id, knowledge: Knowledge, pursuit: Pursuit): Pursuit {
  val targetEnemy = updateTargetEnemy(world, actor, knowledge, pursuit)
  val target = knowledge.characters[pursuit.targetEnemy]
  val actions = if (target != null)
    actionsForTarget(world, actor, target.id)
  else
    null

  val targetPosition = if (target != null && actions != null) {
    target.position ?: target.lastPosition
  } else
    updateRoamingTargetPosition(world, actor, knowledge, pursuit)

  return Pursuit(
      targetEnemy = targetEnemy,
      targetPosition = targetPosition
  )
}

fun updatePursuit(world: World, actor: Id, knowledge: Knowledge, spirit: Spirit): Pursuit {
  val pursuit = spirit.pursuit ?: Pursuit()
  return if (spirit.attributes.contains(SpiritAttributes.isAggressive))
    updateAggression(world, actor, knowledge, pursuit)
  else
    pursuit
}
