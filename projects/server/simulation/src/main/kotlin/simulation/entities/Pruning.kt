package simulation.entities

import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.GameEvent
import silentorb.mythic.physics.Collision
import simulation.main.Hand
import simulation.main.IdHand

typealias CollisionMap = Map<Id, Collision>

data class PruneEntityEvent(
    val id: Id,
    val hand: Hand
) : GameEvent

fun pruningEventsToIdHands(events: Events) =
    events.filterIsInstance<PruneEntityEvent>().map { event ->
      IdHand(
          id = event.id,
          hand = event.hand
      )
    }
