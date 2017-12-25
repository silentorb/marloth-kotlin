package rendering

import spatial.Matrix

class PerspectiveEffect(private val shader: PerspectiveShader, private val camera: Matrix) {
  init {
    shader.cameraMatrix.setValue(camera)
  }

  fun activate() {
    shader.activate()
  }
}

data class EffectsData(
    val cameraMatrix: Matrix
)

data class Effects(
    val standardEffect: PerspectiveEffect
)

fun createEffects(shaderPrograms: Shaders, data: EffectsData): Effects = Effects(
    PerspectiveEffect(shaderPrograms.flat, data.cameraMatrix)
)