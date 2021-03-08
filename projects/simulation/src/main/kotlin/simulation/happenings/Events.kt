package simulation.happenings

import simulation.accessorize.AccessoryName
import silentorb.mythic.ent.Id
import silentorb.mythic.spatial.Vector3

data class PurchaseEvent(
    val customer: Id,
    val merchant: Id,
    val ware: Id,
    val wareType: AccessoryName
)

data class TakeItemEvent(
    val actor: Id,
    val item: Id
)

data class ApplyBuffEvent(
    val buffType: AccessoryName,
    val strength: Int,
    val duration: Int,
    val target: Id,
    val source: Id
)

data class TryActionEvent(
    val actor: Id,
    val action: Id,
    val target: Id? = null,
    val targetLocation: Vector3? = null
)

data class ReturnHome(
    val target: Id
)
