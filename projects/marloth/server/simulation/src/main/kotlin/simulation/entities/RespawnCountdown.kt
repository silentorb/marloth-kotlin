package simulation.entities

import silentorb.mythic.ent.Id
import silentorb.mythic.ent.IdSource
import silentorb.mythic.ent.replacebyKey
import silentorb.mythic.happenings.Events
import simulation.main.Deck
import simulation.main.Hand
import simulation.main.IdHand
import simulation.main.allHandsOnDeck

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
            timer = Timer(
                duration = 3
            ),
            respawnCountdown = RespawnCountdown(
                target = character
            )
        )
    )
  }
}

fun eventsFromRespawnCountdowns(deck: Deck, events: Events): Events {
  return if (!isIntTimerUpdateFrame(events))
    listOf()
  else {
    val expiredCountdowns = deck.respawnCountdowns
        .filter { (id, _) -> deck.timers[id]!!.duration == 0 }

    expiredCountdowns.flatMap { (_, respawner) ->
      listOf(
          RestoreHealth(target = respawner.target)
      )
    }
  }
}
