package lab.utility

import silentorb.mythic.bloom.Depiction
import silentorb.mythic.glowing.cropStack
import silentorb.mythic.glowing.viewportStack

fun embedCameraView(action: Depiction): Depiction = { b, c ->
  val viewport = c.flipViewport(b.toVector4i())
  viewportStack(viewport) {
    cropStack(viewport) {
      action(b, c)
    }
  }
}
