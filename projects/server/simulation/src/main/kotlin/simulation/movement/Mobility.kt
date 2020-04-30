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

private val mobilityCommands = setOf(
    CharacterCommands.moveUp,
    CharacterCommands.moveDown,
    CharacterCommands.moveLeft,
    CharacterCommands.moveRight
)

fun isMobilityCommand(command: CharacterCommand): Boolean =
    mobilityCommands.contains(command.type)

fun newMobilityModifierEvent(actor: Id, source: Id) =
    NewHandEvent(
        hand = Hand(
            modifier = Modifier(
                type = ModifierId.mobile,
                target = actor,
                source = source
            ),
            timerFloat = FloatTimer(3f)
        )
    )

fun mobilityEvents(deck: Deck, commands: List<CharacterCommand>): Events {
  val charactersRequestingMovement = commands
      .filter(::isMobilityCommand)
      .map { it.target }
      .distinct()

  return charactersRequestingMovement
      .filter(canUseMobility(deck))
      .flatMap { actor ->
        val accessory = deck.accessories.entries
            .first { it.value.type == AccessoryId.mobility.name && it.value.owner == actor }

        listOf(
            UseAction(
                actor = actor,
                action = accessory.key
            ),
            newMobilityModifierEvent(actor, accessory.key)
        )
      }
}
