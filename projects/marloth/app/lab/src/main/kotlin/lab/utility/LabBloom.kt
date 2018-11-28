package lab.utility

import mythic.bloom.Depiction
import mythic.bloom.Flower
import mythic.bloom.depict
import mythic.glowing.cropStack
import mythic.glowing.viewportStack

fun embedCameraView(action: Depiction): Depiction = { b, c ->
  val viewport = b.toVector4i()
  viewportStack(viewport) {
    cropStack(viewport) {
      action(b, c)
    }
  }
}

fun depictScene(action: Depiction): Flower =
    depict(embedCameraView(action))
