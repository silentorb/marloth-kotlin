package simulation.misc

import mythic.ent.Entity
import mythic.ent.Id
import simulation.main.Deck

enum class ItemType {
  candle
}

data class Item(
    override val id: Id,
    val owner: Id?,
    val type: ItemType,
    val slot: Int // 0 - 3
) : Entity

fun getItemInSlot(deck: Deck, character: Id, slot: Int): Item? =
    deck.items
        .filterValues { it.owner == character && it.slot == slot }
        .values
        .firstOrNull()

fun equippedItem(deck: Deck): (Id) -> Item? = { characterId ->
  val character = deck.characters[characterId]!!
  deck.items[character.equippedItem]
}

fun isHolding(deck: Deck, player: Id): (ItemType) -> Boolean = { type ->
  equippedItem(deck)(player)?.type == type
}

infix fun Character.equip(item: Id): Character =
    this.copy(
        equippedItem = item
    )

//fun newEntitiesForEquippedItem(deck: Deck, item: Item): Hand {
//  val character = deck.characters[item.owner]!!
//  val characterBody =
//  return Hand(
//      body = Body(
//          id = item.id,
//          position = body.position + direction * 0.5f + Vector3(0f, 0f, 1.4f),
//          node = body.node,
//          shape = commonShapes[EntityType.missile]!!,
//          orientation = Quaternion(),
//          attributes = missileBodyAttributes,
//          gravity = false,
//          perpetual = true
//      )
//  )
//}
