package simulation.happenings

import mythic.ent.Id
import marloth.scenery.enums.ModifierId
import simulation.combat.Damage
import simulation.entities.AccessoryName

interface GameEvent {}

data class DamageEvent(
    val damage: Damage,
    val target: Id
) : GameEvent

data class PurchaseEvent(
    val customer: Id,
    val merchant: Id,
    val ware: Id,
    val wareType: AccessoryName
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

data class TryUseAbilityEvent(
    val actor: Id
) : GameEvent

data class UseAction(
    val actor: Id,
    val action: Id
) : GameEvent

typealias Events = List<GameEvent>
