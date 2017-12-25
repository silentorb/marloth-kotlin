package rendering

import spatial.Matrix
import glowing.MatrixProperty
import glowing.ShaderProgram

class StandardEffect(val shader: ShaderProgram, ) {
  val cameraMatrix = MatrixProperty(shader, "cameraMatrix")
  fun activate(camera: Matrix) {
    shader.activate()
    cameraMatrix.value = camera
  }
}

data class EffectsData(
    val cameraMatrix: Matrix
)

data class Effects(
    val StandardEffect:StandardEffect
)

fun createEffects(data: EffectsData, shaderPrograms: Shaders): Effects {
  return Effects(
      StandardEffect()
  )
}