package simulation

import mythic.spatial.Quaternion
import mythic.spatial.Vector3

class Player(
    val id: Int,
    var position: Vector3 = Vector3(0f, 0f, 0f)
) {
  var orientation = Quaternion()
}