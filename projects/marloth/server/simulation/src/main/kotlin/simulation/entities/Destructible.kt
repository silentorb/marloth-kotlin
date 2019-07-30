package simulation.entities

import mythic.ent.Id
import simulation.combat.*
import simulation.happenings.DamageEvent
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

fun updateDestructibleCache(modifierQuery: DamageModifierQuery): (Id, Destructible) -> Destructible = { id, destructible ->
  val multiplers = calculateDamageMultipliers(modifierQuery, id, destructible.base.damageMultipliers)
  destructible.copy(
      damageMultipliers = multiplers
  )
}

fun updateDestructibleHealth(damages: List<Damage>): (Destructible) -> Destructible = { destructible ->
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

fun updateDestructibleHealth(damageEvents: List<DamageEvent>): (Id, Destructible) -> Destructible = { id, destructible ->
  val damages = damageEvents
      .filter { it.target == id }
      .map { it.damage }

  updateDestructibleHealth(damages)(destructible)
}
