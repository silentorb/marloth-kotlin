package lab.utility

import mythic.bloom.Bounds
import mythic.glowing.viewportStack
import org.joml.Vector2i
import org.joml.Vector4i

fun embedCameraView(b: Bounds, action: (Vector4i) -> Unit) {
  val panelDimensions = Vector2i(b.dimensions.x.toInt(), b.dimensions.y.toInt())
  val viewport = Vector4i(b.position.x.toInt(), b.position.y.toInt(), panelDimensions.x, panelDimensions.y)
  viewportStack(viewport, { action(viewport) })
}