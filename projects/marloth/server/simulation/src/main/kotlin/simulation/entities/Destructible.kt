package simulation.entities

import silentorb.mythic.ent.Id
import simulation.combat.*
import simulation.happenings.DamageEvent
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.GameEvent
import simulation.misc.ResourceContainer
import simulation.misc.modifyResource

data class DestructibleBaseStats(
    val health: Int,
    val damageMultipliers: DamageMultipliers = mapOf()
)

data class Destructible(
    val base: DestructibleBaseStats,
    val health: ResourceContainer,
    val damageMultipliers: DamageMultipliers = mapOf(),
    val lastDamageSource: Id = 0L
)

// This is intended to be used outside of combat.
// It overrides any other health modifying events.
data class RestoreHealth(
    val target: Id
) : GameEvent

fun updateDestructibleCache(modifierQuery: DamageModifierQuery): (Id, Destructible) -> Destructible = { id, destructible ->
  val multiplers = calculateDamageMultipliers(modifierQuery, id, destructible.base.damageMultipliers)
  destructible.copy(
      damageMultipliers = multiplers
  )
}

fun damageDestructible(damages: List<Damage>): (Destructible) -> Destructible = { destructible ->
  if (damages.none()) {
    destructible
  } else {
    val healthMod = aggregateHealthModifiers(destructible, damages)
    val health = modifyResource(destructible.health, healthMod)
    destructible.copy(
        health = destructible.health.copy(value = health),
        lastDamageSource = damages.firstOrNull { it.source != 0L }?.source ?: destructible.lastDamageSource
    )
  }
}

val restoreDestructibleHealth: (Destructible) -> Destructible = { destructible ->
  destructible.copy(
      health = destructible.health.copy(value = destructible.health.max)
  )
}

fun updateDestructibleHealth(events: Events): (Id, Destructible) -> Destructible {
  val damageEvents = events.filterIsInstance<DamageEvent>()
  val restoreEvents = events.filterIsInstance<RestoreHealth>()

  return { id, destructible ->
    if (restoreEvents.any { it.target == id }) {
      restoreDestructibleHealth(destructible)
    } else {
      val damages =
          damageEvents
              .filter { it.target == id }
              .map { it.damage }

      damageDestructible(damages)(destructible)
    }
  }
}
