package simulation

import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import scenery.Depiction

class Body(
    val id: Int,
    val depiction: Depiction,
    var position: Vector3,
    var orientation: Quaternion,
    var velocity: Vector3
)