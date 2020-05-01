package simulation.movement

import marloth.scenery.enums.AccessoryId
import marloth.scenery.enums.ModifierId
import silentorb.mythic.accessorize.Modifier
import silentorb.mythic.characters.Freedom
import silentorb.mythic.characters.FreedomTable
import silentorb.mythic.characters.Freedoms
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import simulation.happenings.canUse
import simulation.main.Deck

fun hasMobilityModifier(modifiers: Table<Modifier>, actor: Id): Boolean =
    modifiers.any { it.value.target == actor && it.value.type == ModifierId.mobile }

fun canUseMobility(deck: Deck): (Id) -> Boolean = { actor ->
  deck.characters[actor]!!.isAlive &&
      deck.performances.none { it.value.target == actor } &&
      deck.accessories.any { it.value.type == AccessoryId.mobility.name && it.value.owner == actor && canUse(deck, it.key) } &&
      !hasMobilityModifier(deck.modifiers, actor)
}

fun getFreedoms(deck: Deck): (Id) -> Freedoms = { actor ->
  val character = deck.characters[actor]!!
  if (!character.isAlive)
    Freedom.none
  else if (deck.performances.any { it.value.target == actor })
    Freedom.orbiting  or Freedom.turning
  else if (!getDebugBoolean("ENABLE_MOBILITY") || hasMobilityModifier(deck.modifiers, actor))
    Freedom.all
  else
    Freedom.orbiting or Freedom.turning
}

// TODO: This is called more than once with the same input data per game loop.  The plan is to cache this.
fun getFreedomTable(deck: Deck): FreedomTable =
    deck.characterRigs.keys.associateWith(getFreedoms(deck))
