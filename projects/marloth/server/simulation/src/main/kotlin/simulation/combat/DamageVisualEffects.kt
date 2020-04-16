package simulation.combat

import marloth.scenery.enums.ParticleEffectType
import silentorb.mythic.combat.general.DamageEvent
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import silentorb.mythic.particles.ParticleEffect
import silentorb.mythic.physics.Body
import silentorb.mythic.timing.FloatTimer
import simulation.main.Deck
import simulation.main.Hand

enum class PlayerOverlayType {
  bleeding,
  dead
}

data class PlayerOverlay(
    val player: Id,
    val type: PlayerOverlayType
)

fun newDamageVisualEffects(deck: Deck, events: Events): List<Hand> {
  val damageGroups = events
      .asSequence()
      .filterIsInstance<DamageEvent>()
      .groupBy { it.target }

  val bloodOverlays = damageGroups
      .filterKeys { character ->
        deck.players.containsKey(character) &&
            deck.playerOverlays.none { it.value.player == character && it.value.type == PlayerOverlayType.bleeding }
      }
      .mapNotNull { (target, damageEvents) ->
        val maxHealth = deck.destructibles[target]!!.base.health
        val degree = damageEvents.sumBy { it.damage.amount }.toFloat() / maxHealth.toFloat()
        if (degree < 0.05f)
          null
        else
          Hand(
              playerOverlay = PlayerOverlay(
                  player = target,
                  type = PlayerOverlayType.bleeding
              ),
              timerFloat = FloatTimer(
                  duration = 20.2f
              )
          )
      }

  val bloodParticleEffects = damageGroups
      .mapNotNull { (target, damageEvents) ->
        Hand(
            body = Body(
                position = damageEvents.firstOrNull { it.position != null }?.position ?: deck.bodies[target]!!.position
            ),
            particleEffect = ParticleEffect(
                type = ParticleEffectType.blood.name
            ),
            timerFloat = FloatTimer(0.4f)
        )
      }
  return bloodOverlays + bloodParticleEffects
}
