package mythic.glowing

import org.lwjgl.BufferUtils
import mythic.spatial.Matrix
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.glBindBufferRange
import org.lwjgl.opengl.GL31.*

private val matrixBuffer = BufferUtils.createFloatBuffer(16)

class MatrixProperty(private val program: ShaderProgram, name: String) {
  private val location = glGetUniformLocation(program.id, name)

  fun setValue(value: Matrix) {
    program.activate()
    glUniformMatrix4fv(location, false, value.get(matrixBuffer))
  }
}

class Vector3Property(private val program: ShaderProgram, name: String) {
  private val location = glGetUniformLocation(program.id, name)

  fun setValue(value: Vector3) {
    program.activate()
    glUniform3f(location, value.x, value.y, value.z)
  }
}

class Vector4Property(private val program: ShaderProgram, name: String) {
  private val location = glGetUniformLocation(program.id, name)

  fun setValue(value: Vector4) {
    program.activate()
    glUniform4f(location, value.x, value.y, value.z, value.w)
  }
}

class UniformBufferProperty(private val program: ShaderProgram, name: String) {
  private val index = glGetUniformBlockIndex(program.id, name)

  init {
    assert(index != -1)
  }

  fun setValue(value: UniformBuffer) {
    glBindBufferRange(GL_UNIFORM_BUFFER, 0, value.id, 0, value.size)
    glUniformBlockBinding(program.id, index, 0)
  }
}

