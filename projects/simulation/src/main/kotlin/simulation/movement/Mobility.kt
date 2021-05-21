package simulation.movement

import marloth.scenery.enums.AccessoryIdOld
import marloth.scenery.enums.CharacterCommands
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Events
import silentorb.mythic.timing.FloatTimer
import simulation.accessorize.Accessory
import simulation.happenings.UseAction
import simulation.main.Deck
import simulation.main.NewHand
import simulation.misc.Definitions

private val mobilityCommands = setOf(
    CharacterCommands.moveUp,
    CharacterCommands.moveDown,
    CharacterCommands.moveLeft,
    CharacterCommands.moveRight
)

fun isMobilityCommand(command: Command): Boolean =
    mobilityCommands.contains(command.type)

fun newMobilityModifierEvent(actor: Id, source: Id, duration: Float) =
    NewHand(
        components = listOf(
            Accessory(
                type = AccessoryIdOld.mobile,
                source = source,
                owner = actor,
            ),
            FloatTimer(duration)
        )
    )

fun mobilityEvents(definitions: Definitions, deck: Deck, commands: List<Command>): Events {
  val charactersRequestingMovement = commands
      .filter(::isMobilityCommand)
      .mapNotNull { it.target as? Id }
      .distinct()

  return charactersRequestingMovement
      .filter(canUseMobility(deck))
      .flatMap { actor ->
        val accessory = deck.accessories.entries
            .first { it.value.type == AccessoryIdOld.mobility && it.value.owner == actor }

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
