package lab.utility

import mythic.bloom.Depiction
import mythic.glowing.cropStack
import mythic.glowing.viewportStack

fun embedCameraView(action: Depiction): Depiction = { b, c ->
  val viewport = c.flipViewport(b.toVector4i())
  viewportStack(viewport) {
    cropStack(viewport) {
      action(b, c)
    }
  }
}
