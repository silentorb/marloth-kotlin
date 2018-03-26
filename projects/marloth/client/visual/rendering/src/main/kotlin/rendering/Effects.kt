package rendering

import mythic.glowing.MatrixProperty
import mythic.spatial.Matrix
import mythic.drawing.DrawingEffects
import mythic.glowing.Texture
import mythic.glowing.UniformBuffer
import mythic.glowing.UniformBufferProperty
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import scenery.Light

class PerspectiveEffect(private val shader: PerspectiveShader, private val camera: CameraEffectsData) {
  private val modelTransform = MatrixProperty(shader.program, "modelTransform")

  init {
    shader.cameraTransform.setValue(camera.transform)
    shader.cameraDirection.setValue(camera.direction)
  }

  fun activate(transform: Matrix) {
    modelTransform.setValue(transform)
    shader.activate()
  }
}

class ColoredPerspectiveEffect(val shader: ColoredPerspectiveShader,
                               camera: CameraEffectsData,
                               sceneBuffer: UniformBuffer) {
  private val perspectiveEffect = PerspectiveEffect(shader.shader, camera)
  private val sceneProperty = UniformBufferProperty(shader.shader.program, "SceneUniform")

  init {
    sceneProperty.setValue(sceneBuffer)
  }

  fun activate(transform: Matrix, color: Vector4, glow: Float, normalTransform: Matrix) {
    shader.activate(color, glow, normalTransform)
    perspectiveEffect.activate(transform)
  }
}

class FlatColoredPerspectiveEffect(val shader: FlatColoredPerspectiveShader, camera: CameraEffectsData) {
  private val perspectiveEffect = PerspectiveEffect(shader.shader, camera)

  fun activate(transform: Matrix, color: Vector4) {
    shader.activate(color)
    perspectiveEffect.activate(transform)
  }
}

class TexturedPerspectiveEffect(private val shader: TextureShader, camera: CameraEffectsData, sceneBuffer: UniformBuffer) {
  private val perspectiveEffect = ColoredPerspectiveEffect(shader.colorShader, camera, sceneBuffer)

  fun activate(transform: Matrix, texture: Texture, color: Vector4, glow: Float, normalTransform: Matrix) {
    shader.activate(texture, color, glow, normalTransform)
    perspectiveEffect.activate(transform, color, glow, normalTransform)
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

fun createEffects(shaders: Shaders, data: EffectsData, sceneBuffer: UniformBuffer): Effects {
  updateLights(data.lights, sceneBuffer)
  return Effects(
      colored = ColoredPerspectiveEffect(shaders.colored, data.camera, sceneBuffer),
      flat = FlatColoredPerspectiveEffect(shaders.flat, data.camera),
      textured = TexturedPerspectiveEffect(shaders.textured, data.camera, sceneBuffer),
      drawing = shaders.drawing
  )
}