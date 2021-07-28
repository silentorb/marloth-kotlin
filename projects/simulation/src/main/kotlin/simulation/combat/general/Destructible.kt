package simulation.combat.general

import silentorb.mythic.ent.Id
import silentorb.mythic.ent.emptyId
import silentorb.mythic.happenings.Events
import simulation.characters.getNourishmentEventsAdjustment
import simulation.characters.getResourceTimeCost
import simulation.characters.getRoundedAccumulation
import simulation.characters.healthTimeDrainDuration
import simulation.main.Deck
import simulation.misc.Definitions
import simulation.misc.highIntScale

data class DestructibleBaseStats(
    val health: Int,
    val damageMultipliers: DamageMultipliers = mapOf()
)


data class Destructible(
    val base: DestructibleBaseStats,
    val health: Int,
    val maxHealth: Int,
    val healthAccumulator: Int = 0,
    val drainDuration: Int = 0,
    val damageMultipliers: DamageMultipliers = mapOf(),
    val lastDamageSource: Id = 0
)

// This is intended to be used outside of combat.
// It overrides any other health modifying events.
data class RestoreHealth(
    val target: Id
)

fun updateDestructibleCache(damageTypes: Set<DamageType>, modifierQuery: DamageModifierQuery): (Id, Destructible) -> Destructible = { id, destructible ->
  val multiplers = calculateDamageMultipliers(damageTypes, modifierQuery, id, destructible.base.damageMultipliers)
  destructible.copy(
      damageMultipliers = multiplers
  )
}

fun damageDestructible(damages: List<Damage>): (Destructible) -> Destructible = { destructible ->
  if (damages.none()) {
    destructible
  } else {
    val healthMod = aggregateHealthModifiers(destructible, damages)
    val nextHealth = modifyResource(destructible.health, destructible.maxHealth, healthMod)
    destructible.copy(
        health = nextHealth,
        lastDamageSource = damages.firstOrNull { it.source != emptyId }?.source ?: destructible.lastDamageSource
    )
  }
}

val restoreDestructibleHealth: (Destructible) -> Destructible = { destructible ->
  destructible.copy(
      health = destructible.maxHealth
  )
}

fun updateDestructibleHealth(definitions: Definitions, deck: Deck, events: Events): (Id, Destructible) -> Destructible {
  val damageEvents = events.filterIsInstance<DamageEvent>()
  val restoreEvents = events.filterIsInstance<RestoreHealth>()

  return { actor, destructible ->
    val result = if (restoreEvents.any { it.target == actor }) {
      restoreDestructibleHealth(destructible)
    } else {
      val damages =
          damageEvents
              .filter { it.target == actor }
              .map { it.damage }

      damageDestructible(damages)(destructible)
    }
    val nourishmentAdjustment = getNourishmentEventsAdjustment(definitions, deck, actor, events)
    val healthAccumulator = if (destructible.drainDuration != 0)
      result.healthAccumulator - getResourceTimeCost(healthTimeDrainDuration, 1)
    else
      result.healthAccumulator

    val healthAccumulation = getRoundedAccumulation(healthAccumulator)
    val mod = healthAccumulation + nourishmentAdjustment
    result.copy(
        health = modifyResourceWithEvents(events, actor, ResourceTypes.health, result.health, result.maxHealth, mod),
        healthAccumulator = healthAccumulator - healthAccumulation * highIntScale,
    )
  }
}
