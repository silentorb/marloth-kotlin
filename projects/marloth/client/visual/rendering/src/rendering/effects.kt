package rendering

import mythic.drawing.ColoredImageEffect
import mythic.glowing.MatrixProperty
import mythic.spatial.Matrix

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
    val coloredImage: ColoredImageEffect
)

fun createEffects(shaderPrograms: Shaders, data: EffectsData): Effects = Effects(
    PerspectiveEffect(shaderPrograms.flat, data.camera),
    ColoredImageEffect(shaderPrograms.coloredImage, data.flatProjection)
)