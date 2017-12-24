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

  var value: Matrix = Matrix()
    set(value) {
//      if (field != value) {
        field = value
        glUniformMatrix4fv(location, false, value.get(matrixBuffer));
//      }
    }
}
