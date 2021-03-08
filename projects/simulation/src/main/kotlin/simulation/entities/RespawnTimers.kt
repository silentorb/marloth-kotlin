package simulation.entities

import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.IdSource
import silentorb.mythic.happenings.Events
import silentorb.mythic.timing.FloatTimer
import simulation.abilities.graveDiggerDurationModifer
import simulation.combat.general.RestoreHealth
import simulation.happenings.ReturnHome
import simulation.main.Deck
import simulation.main.Hand
import simulation.main.IdHand

data class RespawnCountdown(
    val target: Id
)

fun newRespawnCountdown(id: Id, duration: Float, character: Id): IdHand {

  return IdHand(
      id = id,
      hand = Hand(
          respawnCountdown = RespawnCountdown(
              target = character
          ),
          timerFloat = FloatTimer(
              duration = duration
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

  return newlyDeceasedCharacters.map { actor ->
    val id = nextId()
    val duration = if (previous.players.containsKey(actor))
      3f
    else {
      // Use id as a cheap source of random entropy
      15f + (id % 10).toFloat() + graveDiggerDurationModifer(previous, actor)
    }
    newRespawnCountdown(id, duration, actor)
  }
}

fun eventsFromRespawnCountdowns(previous: Deck, next: Deck): Events {
  val expiredCountdowns = previous.respawnCountdowns.keys.minus(next.respawnCountdowns.keys)

  return expiredCountdowns.flatMap { id ->
    val respawner = previous.respawnCountdowns[id]!!
    val target = respawner.target
    val repositionEvent = if (next.players.containsKey(target)) {
      ReturnHome(target = target)
    } else
      null
    listOf(
        RestoreHealth(target = target)
    )
        .plus(listOfNotNull(
            repositionEvent
        ))
  }
}
