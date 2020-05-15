package simulation.characters

import marloth.scenery.enums.AccessoryId
import silentorb.mythic.accessorize.getAccessory
import silentorb.mythic.accessorize.hasAccessory
import silentorb.mythic.ent.Id
import simulation.abilities.dashBonus
import simulation.main.Deck
import simulation.misc.Definitions

typealias MoveSpeedTable = Map<Id, Float>

fun getMoveSpeed(definitions: Definitions, deck: Deck): (Id) -> Float = { actor ->
  val character = deck.characters[actor]!!
  val definition = definitions.professions[character.profession]!!
  val baseSpeed = definition.speed
  val accessory = getAccessory(AccessoryId.dashing, deck.accessories, actor)?.value
  val dashBonus = if (accessory != null)
    dashBonus(accessory.level)
  else
    1f

  baseSpeed * dashBonus
}

fun newMoveSpeedTable(definitions: Definitions, deck: Deck): MoveSpeedTable =
    deck.characterRigs.keys.associateWith(getMoveSpeed(definitions, deck))
