package simulation

import mythic.spatial.Quaternion
import mythic.spatial.Vector3

class Body(
    val id: Int,
    var position: Vector3
) {
  var orientation = Quaternion()
}