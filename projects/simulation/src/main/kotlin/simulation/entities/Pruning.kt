package simulation.entities

import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import silentorb.mythic.physics.Collision
import simulation.main.Hand
import simulation.main.IdHand

typealias CollisionMap = Map<Any, Collision>

data class PruneEntityEvent(
    val id: Id,
    val hand: Hand
)

fun pruningEventsToIdHands(events: Events) =
    events.filterIsInstance<PruneEntityEvent>().map { event ->
      IdHand(
          id = event.id,
          hand = event.hand
      )
    }
