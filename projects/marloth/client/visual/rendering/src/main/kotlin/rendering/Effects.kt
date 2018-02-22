package rendering

import mythic.glowing.MatrixProperty
import mythic.spatial.Matrix
import mythic.drawing.DrawingEffects
import mythic.glowing.Texture
import mythic.glowing.UniformBuffer
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import scenery.Light

class PerspectiveEffect(private val shader: PerspectiveShader, private val camera: CameraEffectsData) {
  val modelTransform = MatrixProperty(shader.program, "modelTransform")

  init {
    shader.cameraTransform.setValue(camera.transform)
    shader.cameraDirection.setValue(camera.direction)
  }

  fun activate(transform: Matrix) {
    modelTransform.setValue(transform)
    shader.activate()
  }
}

class ColoredPerspectiveEffect(val shader: ColoredPerspectiveShader, val camera: CameraEffectsData) {
  val perspectiveEffect = PerspectiveEffect(shader.shader, camera)

  fun activate(transform: Matrix, color: Vector4, normalTransform: Matrix) {
    shader.activate(color, normalTransform)
    perspectiveEffect.activate(transform)
  }
}

class FlatColoredPerspectiveEffect(val shader: FlatColoredPerspectiveShader, camera: CameraEffectsData) {
  val perspectiveEffect = PerspectiveEffect(shader.shader, camera)

  fun activate(transform: Matrix, color: Vector4) {
    shader.activate(color)
    perspectiveEffect.activate(transform)
  }
}

class TexturedPerspectiveEffect(private val shader: TextureShader, camera: CameraEffectsData, sectorBuffer: UniformBuffer) {
  val perspectiveEffect = ColoredPerspectiveEffect(shader.colorShader, camera)

  fun activate(transform: Matrix, texture: Texture, color: Vector4, normalTransform: Matrix) {
    shader.activate(texture, color, normalTransform)
    perspectiveEffect.activate(transform, color, normalTransform)
  }
}

data class CameraEffectsData(
    val transform: Matrix,
    val direction: Vector3
)

data class EffectsData(
    val camera: CameraEffectsData,
    val flatProjection: Matrix,
    val lights: List<Light>
)

data class Effects(
    val colored: ColoredPerspectiveEffect,
    val flat: FlatColoredPerspectiveEffect,
    val textured: TexturedPerspectiveEffect,
    val drawing: DrawingEffects
)

fun createEffects(shaders: Shaders, data: EffectsData, sectorBuffer: UniformBuffer): Effects {
  updateLights(data.lights, sectorBuffer)
  return Effects(
      colored = ColoredPerspectiveEffect(shaders.colored, data.camera),
      flat = FlatColoredPerspectiveEffect(shaders.flat, data.camera),
      textured = TexturedPerspectiveEffect(shaders.textured, data.camera, sectorBuffer),
      drawing = shaders.drawing
  )
}