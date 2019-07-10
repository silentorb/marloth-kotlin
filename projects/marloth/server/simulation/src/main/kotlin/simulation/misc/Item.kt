package simulation.misc

import mythic.ent.Id
import simulation.main.Deck

fun getItemInSlot(deck: Deck, character: Id, slot: Int): Id? =
    deck.attachments
        .filterValues { it.target == character && it.category == AttachmentTypeId.equipped && it.index == slot }
        .keys
        .firstOrNull()

fun equippedItem(deck: Deck): (Id) -> Entity? = { characterId ->
  val character = deck.characters[characterId]!!
  deck.entities[character.equippedItem]
}

fun isHolding(deck: Deck, player: Id): (EntityTypeName) -> Boolean = { type ->
  equippedItem(deck)(player)?.type == type
}

infix fun Character.equip(item: Id): Character =
    this.copy(
        equippedItem = item
    )
