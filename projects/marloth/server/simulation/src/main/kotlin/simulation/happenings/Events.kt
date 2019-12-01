package simulation.happenings

import mythic.ent.Id
import scenery.enums.AccessoryId
import scenery.enums.ModifierId
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

data class TakeItemEvent(
    val actor: Id,
    val item: Id
) : GameEvent

data class ApplyBuffEvent(
    val buffType: ModifierId,
    val strength: Int,
    val duration: Int,
    val target: Id,
    val source: Id
) : GameEvent

data class AttackEvent(
    val actor: Id
) : GameEvent

typealias Events = List<GameEvent>
