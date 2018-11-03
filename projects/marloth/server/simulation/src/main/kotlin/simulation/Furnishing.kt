package simulation

import mythic.ent.Entity
import mythic.ent.Id
import mythic.spatial.Quaternion
import mythic.spatial.Vector3

enum class FurnishingType {
  wallLamp
}

data class Furnishing(
    override val id: Id,
    val type: FurnishingType
) : Entity

data class NewFurnishing(
    val id: Id,
    val type: FurnishingType,
    val position: Vector3,
    val orientation: Quaternion
)