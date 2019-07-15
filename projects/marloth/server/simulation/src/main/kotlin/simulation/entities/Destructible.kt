package simulation.entities

import mythic.ent.Id
import simulation.combat.Damage
import simulation.combat.DamageMultipliers
import simulation.combat.applyDamageMods
import simulation.happenings.DamageEvent
import simulation.misc.Resource
import simulation.misc.modifyResource

data class DestructibleBaseStats(
    val health: Int,
    val damageMultipliers: DamageMultipliers = mapOf()
)

data class Destructible(
    val base: DestructibleBaseStats,
    val health: Resource,
    val damageMultipliers: DamageMultipliers = mapOf(),
    val lastDamageSource: Id = 0L
)

fun aggregateDamage(multipliers: DamageMultipliers, damages: List<Damage>) =
    damages
        .map(applyDamageMods(multipliers))
        .sum()

fun aggregateHealthModifiers(destructible: Destructible, damages: List<Damage>): Int {
  val damage = aggregateDamage(destructible.damageMultipliers, damages)
  return -damage
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
