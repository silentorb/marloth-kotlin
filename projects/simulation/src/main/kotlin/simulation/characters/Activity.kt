package simulation.characters

import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Events
import silentorb.mythic.timing.FloatTimer
import simulation.abilities.sleepingEvent
import simulation.entities.Interaction
import simulation.main.NewHand
import simulation.main.World
import simulation.main.newHandCommand

object CharacterActivity {
  const val idle = "nothing"
  const val sleeping = "sleeping"
  const val startingAbsence = "startingAbsence"
  const val finishingAbsence = "finishingAbsence"
  const val reading = "reading"
}

object ActivityEvents {
  const val startingAbsence = "startingAbsence"
  const val finishingAbsence = "finishingAbsence"
  const val finishedAbsence = "finishedAbsence"
}

fun eventsFromAbsenceStart(nextCommandType: String): (World) -> (Interaction, Id) -> Events = { world ->
  { _, actor ->
    val deck = world.deck
    val character = deck.characters[actor]
    val destructible = deck.destructibles[actor]
    if (character == null || destructible == null)
      listOf()
    else
      listOf(
          Command(ActivityEvents.startingAbsence, target = actor),
          NewHand(
              components = listOf(
                  FloatTimer(1f, onFinished = listOf(
                      Command(ActivityEvents.finishingAbsence, target = actor),
                      Command(nextCommandType, target = actor),
                  ))
              )
          )
      )
  }
}

fun finishAbsence(actor: Id) =
    Command(
        type = newHandCommand,
        value = NewHand(
            components = listOf(
                FloatTimer(1f, onFinished = listOf(
                    Command(ActivityEvents.finishedAbsence, target = actor),
                ))
            )
        )
    )
