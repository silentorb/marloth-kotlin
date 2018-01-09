package mythic.glowing

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER
import org.lwjgl.opengl.GL15.glBindBuffer
import org.lwjgl.opengl.GL20.glUseProgram
import org.lwjgl.opengl.GL30.glBindVertexArray

import mythic.spatial.Vector4
import org.joml.Vector4i
import org.lwjgl.opengl.GL14.GL_BLEND_DST_RGB
import org.lwjgl.opengl.GL14.GL_BLEND_SRC_RGB

private fun getBounds(type: Int): Vector4i {
  val buffer = IntArray(4)
  glGetIntegerv(type, buffer)
  return Vector4i(buffer[0], buffer[1], buffer[2], buffer[3])
}

private fun setEnabled(register: Int, value: Boolean) {
  if (value)
    glEnable(register)
  else
    glDisable(register)
}

class State {

  var clearColor: Vector4 = Vector4(0f, 0f, 0f, 1f)
    set(value) {
      if (field != value) {
        field = value
        glClearColor(value.x, value.y, value.z, value.w)
      }
    }

  var vertexArrayObject: Int = 0
    set(value) {
      if (field != value) {
        field = value
        glBindVertexArray(value)
      }
    }

  var vertexBufferObject: Int = 0
    set(value) {
      if (field != value) {
        field = value
        glBindBuffer(GL_ARRAY_BUFFER, value)
      }
    }

  var shaderProgram: Int = 0
    set(value) {
      if (field != value) {
        field = value
        glUseProgram(value)
      }
    }

  var lineThickness: Float = 1f
    set(value) {
      if (field != value) {
        field = value
        glLineWidth(value)
      }
    }

  var viewport: Vector4i = getBounds(GL_VIEWPORT)
    set(value) {
      if (field != value) {
        field = value
        glViewport(value.x, value.y, value.z, value.w)
      }
    }

  var cropBounds: Vector4i = getBounds(GL_SCISSOR_BOX)
    set(value) {
      if (field != value) {
        field = value
        glScissor(value.x, value.y, value.z, value.w)
      }
    }

  var cropEnabled: Boolean = false
    set(value) {
      if (field != value) {
        field = value
        setEnabled(GL_SCISSOR_TEST, value)
      }
    }

  var blendEnabled: Boolean = false
    set(value) {
      if (field != value) {
        field = value
        setEnabled(GL_BLEND, value)
      }
    }

  var blendFunction: Pair<Int, Int> = Pair(glGetInteger(GL_BLEND_SRC_RGB), glGetInteger(GL_BLEND_DST_RGB))
    set(value) {
      if (field != value) {
        field = value
        glBlendFunc(value.first, value.second)
      }
    }

}

val globalState = State()