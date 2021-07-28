package simulation.abilities

import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import simulation.characters.maxSanity
import simulation.combat.general.ModifyResource
import simulation.combat.general.ResourceTypes
import simulation.entities.Interaction
import simulation.main.World
import kotlin.math.max

fun eventsFromDevotion(world: World): (Interaction, Id) -> Events = { _, actor ->
  val deck = world.deck
  val character = deck.characters[actor]
  if (character == null)
    listOf()
  else {
    val gainedSanity = max(0, maxSanity - character.sanity)
    val flatExpense = 20
    val timeExpense = gainedSanity / 10
    val totalExpense = flatExpense + timeExpense
    val splitTotal = totalExpense / 2
    listOf(
        ModifyResource(
            actor = actor,
            resource = ResourceTypes.sanity,
            amount = gainedSanity,
        ),
        ModifyResource(
            actor = actor,
            resource = ResourceTypes.health,
            amount = -splitTotal,
        ),
        ModifyResource(
            actor = actor,
            resource = ResourceTypes.health,
            amount = -splitTotal,
        ),
    )
  }
}
