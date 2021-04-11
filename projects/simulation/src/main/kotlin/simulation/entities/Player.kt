package simulation.entities

import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Events

data class Player(
    val name: String,
    val rig: Id,
)

data class PlayerRigEvent(
    val player: Id,
    val rig: Id,
)

fun updatePlayer(events: Events): (Id, Player) -> Player {
  val playerEvents = events.filterIsInstance<PlayerRigEvent>()
  return { id, player ->
    val event = playerEvents.firstOrNull { it.player == id }
    if (event != null)
      player.copy(
          rig = event.rig,
      )
    else
      player
  }
}

fun remapPlayerRigCommands(players: Table<Player>, events: Events): Events {
  val remappedRigs = players
      .filter { (key, value) ->
        value.rig != key
      }

  return if (remappedRigs.none())
    events
  else {
    events.map { event ->
      val command = event as? Command
      if (command == null)
        event
      else {
        val rig = remappedRigs[command.target]?.rig
        if (rig != null)
          command.copy(
              target = rig,
          )
        else
          event
      }
    }
  }
}
