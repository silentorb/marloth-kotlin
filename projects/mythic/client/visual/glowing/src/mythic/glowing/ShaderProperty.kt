package mythic.glowing

import org.lwjgl.BufferUtils
import mythic.spatial.Matrix
import mythic.spatial.Vector4
import org.lwjgl.opengl.GL20.*

private val matrixBuffer = BufferUtils.createFloatBuffer(16)

class MatrixProperty(private val program: ShaderProgram, name: String) {
  private val location = glGetUniformLocation(program.id, name)

  fun setValue(value: Matrix) {
    program.activate()
    glUniformMatrix4fv(location, false, value.get(matrixBuffer))
  }
}

class Vector4Property(private val program: ShaderProgram, name: String) {
  private val location = glGetUniformLocation(program.id, name)

  fun setValue(value: Vector4) {
    program.activate()
    glUniform4f(location, value.x, value.y, value.z, value.w)
  }
}