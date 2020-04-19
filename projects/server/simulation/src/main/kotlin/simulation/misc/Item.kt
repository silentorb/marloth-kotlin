package simulation.misc

import silentorb.mythic.accessorize.Accessory
import silentorb.mythic.accessorize.AccessoryName
import silentorb.mythic.ent.Id
import simulation.entities.AttachmentCategory
import simulation.main.Deck

fun getItemInSlot(deck: Deck, character: Id, slot: Int): Id? =
    deck.attachments
        .filterValues { it.target == character && it.category == AttachmentCategory.equipped && it.index == slot }
        .keys
        .firstOrNull()

fun equippedItem(deck: Deck): (Id) -> Accessory? = { characterId ->
  val character = deck.characters[characterId]!!
  deck.accessories[character.activeAccessory]
}

fun isHolding(deck: Deck, player: Id): (AccessoryName) -> Boolean = { type ->
  equippedItem(deck)(player)?.type == type
}

fun hasEquipped(deck: Deck, character: Id): (AccessoryName) -> Boolean = { type ->
  deck.accessories
      .filterValues { it.owner == character }
      .any { deck.accessories[it.key]?.type == type }
}
