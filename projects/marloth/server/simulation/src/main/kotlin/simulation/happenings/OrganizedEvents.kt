package simulation.happenings

import mythic.ent.Id
import simulation.combat.Damage
import simulation.main.DeckSource

interface GameEvent {}

data class DamageEvent(
    val damage: Damage,
    val target: Id
) : GameEvent

data class PurchaseEvent(
    val customer: Id,
    val merchant: Id,
    val ware: Id
) : GameEvent

data class DeckEvent(
    val deck: DeckSource
) : GameEvent

data class OrganizedEvents(
    val damage: List<DamageEvent> = listOf(),
    val decks: List<DeckSource> = listOf(),
    val purchases: List<PurchaseEvent> = listOf()
)

typealias Events = List<GameEvent>
