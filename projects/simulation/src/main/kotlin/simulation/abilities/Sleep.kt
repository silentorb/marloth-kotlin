package simulation.abilities

import marloth.scenery.enums.CharacterCommands
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Commands
import silentorb.mythic.happenings.Events
import simulation.combat.general.ModifyResource
import simulation.combat.general.ResourceTypes
import simulation.entities.Interaction
import simulation.entities.Interactions
import simulation.main.World
import kotlin.math.max

fun eventsFromSleep(world: World): (Interaction, Id) -> Events = { _, actor ->
  val deck = world.deck
  val character = deck.characters[actor]
  val destructible = deck.destructibles[actor]
  if (character == null || destructible == null)
    listOf()
  else {
    destructible.health

    val gainedEnergy = max(0, destructible.health - character.energy)
    val familyExpense = 10
    val energyExpense = gainedEnergy / 10
    val totalExpense = familyExpense + energyExpense
    listOf(
        ModifyResource(
            actor = actor,
            resource = ResourceTypes.energy,
            amount = gainedEnergy,
        ),
        ModifyResource(
            actor = actor,
            resource = ResourceTypes.health,
            amount = -totalExpense,
        )
    )
  }
}

fun nextCommandsFromSleep(events: Events): Commands =
    if (events.filterIsInstance<Interaction>().any { it.type == Interactions.sleep })
      listOf(
          Command(
              type = CharacterCommands.nextWorld,
          )
      )
    else
      listOf()
