package rendering

import mythic.glowing.MatrixProperty
import mythic.spatial.Matrix
import mythic.drawing.DrawingEffects
import mythic.glowing.Texture
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import mythic.spatial.Vector4

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

class FlatColoredPerspectiveEffect(val shader: FlatColoredPerspectiveShader, camera: Matrix){
  val perspectiveEffect = PerspectiveEffect(shader.shader, camera)

  fun activate(transform: Matrix, color: Vector4){
    shader.activate(color)
    perspectiveEffect.activate(transform)
  }
}

class TexturedPerspectiveEffect(private val shader: TextureShader, private val camera: Matrix) {
  val modelTransform = MatrixProperty(shader.program, "modelTransform")
  val normalTransform = MatrixProperty(shader.program, "normalTransform")

  init {
    val cameraDirection = camera.getUnnormalizedRotation(Quaternion()).transform(Vector3(1f, 0f, 0f))
    shader.cameraTransform.setValue(camera)
    shader.cameraDirection.setValue(cameraDirection)
  }

  fun activate(transform: Matrix, texture: Texture) {
    modelTransform.setValue(transform)
    normalTransform.setValue(Matrix())
    shader.activate(texture)
  }
}

data class EffectsData(
    val camera: Matrix,
    val flatProjection: Matrix
)

data class Effects(
    val flat: FlatColoredPerspectiveEffect,
    val textured: TexturedPerspectiveEffect,
    val drawing: DrawingEffects
)

fun createEffects(shaders: Shaders, data: EffectsData): Effects = Effects(
    flat = FlatColoredPerspectiveEffect(shaders.flat, data.camera),
    textured = TexturedPerspectiveEffect(shaders.textured, data.camera),
    drawing = shaders.drawing
)