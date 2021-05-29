package simulation.combat

import marloth.scenery.enums.ParticleEffectType
import simulation.combat.general.DamageEvent
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import silentorb.mythic.particles.ParticleEffect
import silentorb.mythic.physics.Body
import silentorb.mythic.timing.FloatTimer
import simulation.main.Deck
import simulation.main.NewHand
import kotlin.math.max

enum class PlayerOverlayType {
  bleeding,
  dead
}

data class PlayerOverlay(
    val player: Id,
    val type: PlayerOverlayType,
    val strength: Float = 1f,
)

fun newDamageVisualEffects(deck: Deck, events: Events): List<NewHand> {
  val damageGroups = events
      .asSequence()
      .filterIsInstance<DamageEvent>()
      .groupBy { it.target }

  val bloodOverlays = damageGroups
      .filterKeys { character ->
        deck.players.containsKey(character) &&
            deck.playerOverlays.none { it.value.player == character && it.value.type == PlayerOverlayType.bleeding }
      }
      .map { (target, damageEvents) ->
        val maxHealth = deck.destructibles[target]!!.base.health
        val strength = max(0.1f, damageEvents.sumBy { it.damage.amount }.toFloat() / maxHealth.toFloat())
        NewHand(
            components = listOf(
                PlayerOverlay(
                    player = target,
                    type = PlayerOverlayType.bleeding,
                    strength = strength,
                ),
                FloatTimer(
                    duration = 0.2f
                )
            )
        )
      }

  val bloodParticleEffects = damageGroups
      .mapNotNull { (target, damageEvents) ->
        NewHand(
            components = listOf(Body(
                position = damageEvents.firstOrNull { it.position != null }?.position ?: deck.bodies[target]!!.position
            ),
                ParticleEffect(
                    type = ParticleEffectType.blood.name
                ),
                FloatTimer(0.9f)
            )
        )
      }
  return bloodOverlays + bloodParticleEffects
}
