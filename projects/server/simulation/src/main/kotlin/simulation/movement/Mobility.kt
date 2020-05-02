package simulation.movement

import marloth.scenery.enums.AccessoryId
import marloth.scenery.enums.CharacterCommands
import marloth.scenery.enums.ModifierId
import silentorb.mythic.accessorize.Modifier
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.CharacterCommand
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.UseAction
import silentorb.mythic.timing.FloatTimer
import simulation.happenings.NewHandEvent
import simulation.main.Deck
import simulation.main.Hand
import simulation.misc.Definitions

private val mobilityCommands = setOf(
    CharacterCommands.moveUp,
    CharacterCommands.moveDown,
    CharacterCommands.moveLeft,
    CharacterCommands.moveRight
)

fun isMobilityCommand(command: CharacterCommand): Boolean =
    mobilityCommands.contains(command.type)

fun newMobilityModifierEvent(actor: Id, source: Id, duration: Float) =
    NewHandEvent(
        hand = Hand(
            modifier = Modifier(
                type = ModifierId.mobile,
                target = actor,
                source = source
            ),
            timerFloat = FloatTimer(duration)
        )
    )

fun mobilityEvents(definitions: Definitions, deck: Deck, commands: List<CharacterCommand>): Events {
  val charactersRequestingMovement = commands
      .filter(::isMobilityCommand)
      .map { it.target }
      .distinct()

  return charactersRequestingMovement
      .filter(canUseMobility(deck))
      .flatMap { actor ->
        val accessory = deck.accessories.entries
            .first { it.value.type == AccessoryId.mobility && it.value.owner == actor }

        val duration = definitions.actions[accessory.value.type]!!.duration

        listOf(
            UseAction(
                actor = actor,
                action = accessory.key
            ),
            newMobilityModifierEvent(actor, accessory.key, duration)
        )
      }
}
