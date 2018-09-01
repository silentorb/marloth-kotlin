package simulation

import mythic.spatial.Quaternion
import mythic.spatial.Vector3

enum class FurnishingType {
  wallLamp
}

data class Furnishing(
    override val id: Id,
    val type: FurnishingType
) : EntityLike

data class NewFurnishing(
    val id: Id,
    val type: FurnishingType,
    val position: Vector3,
    val orientation: Quaternion
)