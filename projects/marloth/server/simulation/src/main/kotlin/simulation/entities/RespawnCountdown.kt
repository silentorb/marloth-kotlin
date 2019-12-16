package simulation.entities

import silentorb.mythic.ent.Id
import silentorb.mythic.ent.IdSource
import silentorb.mythic.ent.replacebyKey
import simulation.main.Deck
import simulation.main.Hand
import simulation.main.allHandsOnDeck

data class RespawnCountdown(
    val target: Id
)

// Should be performed after destructibles are updated and before entities are removed
fun createRespawnCountdowns(nextId: IdSource): (Deck) -> Deck = { deck ->
  val newlyDeceased = deck.destructibles.filter { (key, destructible) ->
    destructible.health.value == 0 && deck.players.containsKey(key)
  }.keys
  if (newlyDeceased.none())
    deck
  else {
    val hands = newlyDeceased.map { character ->
      Hand(
          timer = Timer(
              duration = 3
          ),
          respawnCountdown = RespawnCountdown(
              target = character
          )
      )
    }
    allHandsOnDeck(hands, nextId, deck)
  }
}

val applyFinishedRespawnCountdowns: (Deck) -> Deck = { deck ->
  val expiredCountdowns = deck.respawnCountdowns.keys
      .filter { id -> deck.timers[id]!!.duration == 0 }
      .toSet()
  if (expiredCountdowns.none())
    deck
  else {
    val destructibles = replacebyKey<Id, Destructible>(expiredCountdowns)(deck.destructibles) { key, value ->
      value.copy(
          health = value.health.copy(
              value = value.base.health
          )
      )
    }
    deck.copy(
        //        respawnCountdowns = deck.respawnCountdowns.minus(expiredCountdowns),
        destructibles = destructibles
    )
  }
}
