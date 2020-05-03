package simulation.happenings

import silentorb.mythic.accessorize.AccessoryName
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.GameEvent
import silentorb.mythic.spatial.Vector3

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
    val buffType: AccessoryName,
    val strength: Int,
    val duration: Int,
    val target: Id,
    val source: Id
) : GameEvent

data class TryActionEvent(
    val actor: Id,
    val action: Id,
    val target: Id? = null,
    val targetLocation: Vector3? = null
) : GameEvent

data class ReturnHome(
    val target: Id
) : GameEvent
