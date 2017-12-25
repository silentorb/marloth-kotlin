package glowing

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL20.glGetUniformLocation
import org.lwjgl.opengl.GL20.glUniformMatrix4fv
import spatial.Matrix

private val matrixBuffer = BufferUtils.createFloatBuffer(16)

class ShaderProperty {

}

class MatrixProperty(program: ShaderProgram, name: String) {
  private val location = glGetUniformLocation(program.id, name)

  fun setValue(value: Matrix) {
    glUniformMatrix4fv(location, false, value.get(matrixBuffer));
  }
}
