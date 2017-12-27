package rendering

import glowing.MatrixProperty
import spatial.Matrix

class PerspectiveEffect(private val shader: PerspectiveShader, private val camera: Matrix) {
  val modelTransform = MatrixProperty(shader.program, "modelTransform")

  init {
    shader.cameraTransform.setValue(camera)
  }

  fun activate(transform: Matrix) {
    modelTransform.setValue(transform)
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