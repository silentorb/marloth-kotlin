package simulation.happenings

import mythic.ent.Id
import scenery.enums.AccessoryId
import simulation.combat.Damage
import simulation.main.DeckSource
import simulation.misc.ResourceMap

interface GameEvent {}

data class DamageEvent(
    val damage: Damage,
    val target: Id
) : GameEvent

data class PurchaseEvent(
    val customer: Id,
    val merchant: Id,
    val ware: Id,
    val wareType: AccessoryId
) : GameEvent

data class DeckEvent(
    val deck: DeckSource
) : GameEvent

data class TakeItemEvent(
    val actor: Id,
    val item: Id
)

data class OrganizedEvents(
    val damage: List<DamageEvent> = listOf(),
    val decks: List<DeckSource> = listOf(),
    val takeItems: List<TakeItemEvent> = listOf(),
    val purchases: List<PurchaseEvent> = listOf()
)

typealias Events = List<GameEvent>
