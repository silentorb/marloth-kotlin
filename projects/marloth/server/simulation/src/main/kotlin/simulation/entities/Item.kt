package simulation.entities

import mythic.ent.Id
import scenery.enums.AccessoryId
import simulation.main.Deck

fun getItemInSlot(deck: Deck, character: Id, slot: Int): Id? =
    deck.attachments
        .filterValues { it.target == character && it.category == AttachmentTypeId.equipped && it.index == slot }
        .keys
        .firstOrNull()

fun equippedItem(deck: Deck): (Id) -> Accessory? = { characterId ->
  val character = deck.characters[characterId]!!
  deck.accessories[character.activeItem]
}

fun isHolding(deck: Deck, player: Id): (AccessoryId) -> Boolean = { type ->
  equippedItem(deck)(player)?.type == type
}

fun hasEquipped(deck: Deck, character: Id): (AccessoryId) -> Boolean = { type ->
  deck.attachments
      .filterValues { it.target == character && it.category == AttachmentTypeId.equipped }
      .any { deck.accessories[it.key]?.type == type }
}

infix fun Character.equip(item: Id): Character =
    this.copy(
        activeItem = item
    )
