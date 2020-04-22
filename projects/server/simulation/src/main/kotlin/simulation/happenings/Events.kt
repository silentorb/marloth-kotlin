package simulation.happenings

import marloth.scenery.enums.ModifierId
import silentorb.mythic.accessorize.AccessoryName
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.GameEvent

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

data class ReturnHome(
    val target: Id
) : GameEvent
