package simulation.entities

import silentorb.mythic.combat.general.DamageEvent
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
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

fun newPlayerOverlays(deck: Deck, events: Events): List<Hand> {
  return events
      .asSequence()
      .filterIsInstance<DamageEvent>()
      .filter { deck.players.containsKey(it.target) }
      .filter { deck.playerOverlays[it.target]?.type != PlayerOverlayType.bleeding }
      .groupBy { it.target }
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
                  duration = 0.2f
              )
          )
      }
}
