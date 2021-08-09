package simulation.abilities

import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Commands
import silentorb.mythic.happenings.Events
import simulation.characters.finishAbsence
import simulation.entities.Interaction
import simulation.entities.InteractionActions
import simulation.macro.newMacroUpdate
import simulation.main.hours
import simulation.main.World

const val sleepingEvent = "sleeping"

fun eventsFromSleeping(world: World): (Command, Id) -> Events = { _, actor ->
  val deck = world.deck
  val character = deck.characters[actor]
  val destructible = deck.destructibles[actor]
  if (character == null || destructible == null)
    listOf()
  else {
    val duration = 8 * hours
    listOf(
        newMacroUpdate(duration, listOf(
            finishAbsence(actor),
        )),
    )
  }
}

fun nextCommandsFromSleep(events: Events): Commands =
    if (events.filterIsInstance<Interaction>().any { it.type == InteractionActions.sleep })
      listOf(
//          Command(
//              type = CharacterCommands.nextWorld,
//          )
      )
    else
      listOf()
