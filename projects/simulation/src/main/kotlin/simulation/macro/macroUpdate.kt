package simulation.macro

import silentorb.mythic.ent.Id
import silentorb.mythic.ent.mapTable
import silentorb.mythic.happenings.Events
import simulation.accessorize.Accessory
import simulation.accessorize.IntrinsicReplenishment
import simulation.accessorize.limitQuantity
import simulation.accessorize.replenishmentKey
import simulation.characters.Character
import simulation.characters.CharacterActivity
import simulation.characters.updateEnergy
import simulation.combat.general.Destructible
import simulation.combat.general.ModifyResource
import simulation.combat.general.ResourceTypes
import simulation.combat.general.modifyDestructible
import simulation.main.Deck
import simulation.main.Frames
import simulation.main.World
import simulation.misc.Definitions
import kotlin.math.max
import kotlin.math.min

fun gatherMacroEvents(deck: Deck): Events {
  val sleepingCharacters = deck.characters
      .filterValues { it.activity == CharacterActivity.finishingAbsence }

  return sleepingCharacters
      .flatMap { (actor, character) ->
        val destructible = deck.destructibles[actor]
        val energyMax = destructible?.health ?: 100
        val gainedEnergy = max(0, energyMax - character.energy)
        val flatExpense = 20
        val timeExpense = gainedEnergy / 10
        val totalExpense = flatExpense + timeExpense
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
            ),
        )
      }
}

fun updateAccessoryMacro(): (Id, Accessory) -> Accessory {
  return { _, accessory ->
    val components = accessory.components
    val replenishment = components[replenishmentKey] as? IntrinsicReplenishment
    if (replenishment != null) {
      val quantity = accessory.quantity
      val counter = replenishment.counter
      val nextCounter = if (quantity == 0 && counter > replenishment.delay)
        0
      else
        counter + 1

      val nextQuantity = if (counter >= replenishment.delay || quantity > 0)
        limitQuantity(accessory.maxQuantity, quantity + 1)
      else
        quantity

      accessory.copy(
          components = components + (replenishmentKey to replenishment.copy(counter = nextCounter)),
          quantity = nextQuantity
      )
    } else
      accessory
  }
}

fun updateDestructibleMacro(events: Events): (Id, Destructible) -> Destructible {
  return { actor, destructible ->
    destructible.copy(
        health = modifyDestructible(events, actor, destructible)
    )
  }
}

fun updateCharacterMacro(deck: Deck, events: Events): (Id, Character) -> Character {
  return { actor, character ->
    val destructible = deck.destructibles[actor]
    if (destructible == null)
      character
    else
      character.copy(
          energy = updateEnergy(events, actor, character, destructible)
      )
  }
}

fun updateDeckMacro(frames: Frames, definitions: Definitions, events: Events, deck: Deck) =
    deck.copy(
        accessories = mapTable(deck.accessories, updateAccessoryMacro()),
        characters = mapTable(deck.characters, updateCharacterMacro(deck, events)),
        destructibles = mapTable(deck.destructibles, updateDestructibleMacro(events)),
    )

fun updateMacro(frames: Frames, world: World): World {
  val deck = world.deck
  val events = gatherMacroEvents(deck)
  val nextDeck = updateDeckMacro(frames, world.definitions, events, deck)
  return world.copy(
      deck = nextDeck,
  )
}
