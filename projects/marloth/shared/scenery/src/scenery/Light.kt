package scenery

import mythic.spatial.Vector3
import mythic.spatial.Vector4

enum class LightType(val value: Int) {
  point(1),
  spot(2)
}

data class Light(
    var type: LightType,
    var color: Vector4, // w is brightness
    var position: Vector3,
    var direction: Vector4 = Vector4(0f) // w is range
)
