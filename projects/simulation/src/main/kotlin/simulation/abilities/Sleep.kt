package simulation.abilities

import marloth.scenery.enums.CharacterCommands
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Commands
import silentorb.mythic.happenings.Events
import simulation.combat.general.ModifyResource
import simulation.combat.general.ResourceTypes
import simulation.main.World
import kotlin.math.max

fun eventsFromSleep(world: World): (Command, Id) -> Events = { command, actor ->
  val deck = world.deck
  val character = deck.characters[actor]
  val destructible = deck.destructibles[actor]
  if (character == null || destructible == null)
    listOf()
  else {
    destructible.health

    listOf(
        ModifyResource(
            actor = actor,
            resource = ResourceTypes.energy,
            amount = max(0, destructible.health - character.energy),
        ),
        ModifyResource(
            actor = actor,
            resource = ResourceTypes.health,
            amount = -20,
        )
    )
  }
}

fun nextCommandsFromSleep(commands: Commands): Commands =
    if (commands.any { it.type == CharacterCommands.sleep })
      listOf(
          Command(
              type = CharacterCommands.nextWorld,
          )
      )
    else
      listOf()
