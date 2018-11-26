package lab.utility

import mythic.bloom.Bounds
import mythic.bloom.Flower
import mythic.bloom.depict
import mythic.glowing.cropStack
import mythic.glowing.viewportStack
import org.joml.Vector2i
import org.joml.Vector4i

fun embedCameraView(b: Bounds, action: (Vector4i) -> Unit) {
  val panelDimensions = Vector2i(b.dimensions.x, b.dimensions.y)
  val viewport = Vector4i(b.position.x, b.position.y, panelDimensions.x, panelDimensions.y)
  viewportStack(viewport) {
    cropStack(viewport) {
      action(viewport)
    }
  }
}

fun depictScene(action: (Vector4i) -> Unit): Flower =
    depict { embedCameraView(it, action) }
