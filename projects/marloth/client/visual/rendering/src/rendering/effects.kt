package rendering

import mythic.drawing.Canvas
import mythic.glowing.MatrixProperty
import mythic.spatial.Matrix
import mythic.drawing.DrawingEffects

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
    val camera: Matrix,
    val flatProjection: Matrix
)

data class Effects(
    val standard: PerspectiveEffect,
    val drawing: DrawingEffects
)

fun createEffects(shaderPrograms: Shaders, data: EffectsData): Effects = Effects(
    PerspectiveEffect(shaderPrograms.flat, data.camera),
    shaderPrograms.drawing
)