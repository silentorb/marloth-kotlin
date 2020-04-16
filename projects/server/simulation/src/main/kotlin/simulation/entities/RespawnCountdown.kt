package simulation.entities

import silentorb.mythic.combat.general.RestoreHealth
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.IdSource
import silentorb.mythic.happenings.Events
import silentorb.mythic.timing.FloatTimer
import simulation.main.Deck
import simulation.main.Hand
import simulation.main.IdHand

data class RespawnCountdown(
    val target: Id
)

fun newRespawnCountdown(nextId: IdSource): (Id) -> IdHand = { character ->
  val id = nextId()
  // Use the id as a cheap source of random entropy
  val durationVariance = (id % 30).toFloat() / 10f
  IdHand(
      id = id,
      hand = Hand(
          timerFloat = FloatTimer(
              duration = 3f + durationVariance
          ),
          respawnCountdown = RespawnCountdown(
              target = character
          )
      )
  )
}

// Should be performed after destructibles are updated and before entities are removed
fun newRespawnCountdowns(nextId: IdSource, previous: Deck, next: Deck): List<IdHand> {
  val newlyDeceasedCharacters = previous.characters.keys
      .filter { id ->
        previous.characters[id]!!.isAlive && !next.characters[id]!!.isAlive
      }
      .filter { id ->
        !previous.players.containsKey(id) || getDebugBoolean("PLAYER_RESPAWN")
      }

  return newlyDeceasedCharacters.map(newRespawnCountdown(nextId))
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
