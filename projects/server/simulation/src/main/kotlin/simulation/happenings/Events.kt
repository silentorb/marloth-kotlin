package simulation.happenings

import silentorb.mythic.ent.Id
import marloth.scenery.enums.ModifierId
import silentorb.mythic.happenings.GameEvent
import silentorb.mythic.accessorize.AccessoryName

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

