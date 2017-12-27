package simulation

import spatial.Quaternion
import spatial.Vector3

class Player(val id: Int) {
  var position = Vector3(-10f, 0f, 0f)
  var orientation = Quaternion()
}