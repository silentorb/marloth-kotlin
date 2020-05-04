package simulation.movement

import marloth.scenery.enums.AccessoryId
import silentorb.mythic.accessorize.Modifier
import silentorb.mythic.characters.rigs.Freedom
import silentorb.mythic.characters.rigs.FreedomTable
import silentorb.mythic.characters.rigs.Freedoms
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.performing.isPerforming
import simulation.abilities.isEntangled
import simulation.happenings.canUseSimple
import simulation.main.Deck

fun hasMobilityModifier(modifiers: Table<Modifier>, actor: Id): Boolean =
    modifiers.any { it.value.target == actor && it.value.type == AccessoryId.mobile }

fun canUseMobility(deck: Deck): (Id) -> Boolean = { actor ->
  deck.characters[actor]!!.isAlive &&
      deck.performances.none { it.value.target == actor } &&
      deck.accessories.any { it.value.type == AccessoryId.mobility && it.value.owner == actor && canUseSimple(deck, it.key) } &&
      !hasMobilityModifier(deck.modifiers, actor)
}

fun getFreedoms(deck: Deck): (Id) -> Freedoms = { actor ->
  val character = deck.characters[actor]!!
  if (!character.isAlive)
    Freedom.none
  else {
    val isPerforming = isPerforming(deck.performances, actor)
    val canWalk = !isPerforming && !isEntangled(deck.accessories, actor) && (!getDebugBoolean("ENABLE_MOBILITY") || hasMobilityModifier(deck.modifiers, actor))
    val walking =if (canWalk) Freedom.walking else Freedom.none
    val acting = if (!isPerforming) Freedom.acting else Freedom.none
    Freedom.orbiting or Freedom.turning or walking or acting
  }
}

// TODO: This is called more than once with the same input data per game loop.  The plan is to cache this.
fun getFreedomTable(deck: Deck): FreedomTable =
    deck.characterRigs.keys.associateWith(getFreedoms(deck))
