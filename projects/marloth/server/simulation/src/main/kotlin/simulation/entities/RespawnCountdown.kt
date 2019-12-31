package simulation.entities

import silentorb.mythic.combat.general.RestoreHealth
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.IdSource
import silentorb.mythic.happenings.Events
import silentorb.mythic.timing.IntTimer
import simulation.main.Deck
import simulation.main.Hand
import simulation.main.IdHand

data class RespawnCountdown(
    val target: Id
)

// Should be performed after destructibles are updated and before entities are removed
fun newRespawnCountdowns(nextId: IdSource, previous: Deck, next: Deck): List<IdHand> {
  val newlyDeceased = previous.players.keys.filter { id ->
    previous.characters[id]!!.isAlive && !next.characters[id]!!.isAlive
  }
  return newlyDeceased.map { character ->
    IdHand(
        id = nextId(),
        hand = Hand(
            timer = IntTimer(
                duration = 3,
                interval = 60
            ),
            respawnCountdown = RespawnCountdown(
                target = character
            )
        )
    )
  }
}

fun eventsFromRespawnCountdowns(previous: Deck, next: Deck, events: Events): Events {
  val expiredCountdowns = previous.respawnCountdowns.keys.minus(next.respawnCountdowns.keys)

  return expiredCountdowns.flatMap { id ->
    val respawner = previous.respawnCountdowns[id]!!
    listOf(
        RestoreHealth(target = respawner.target)
    )
  }
}
