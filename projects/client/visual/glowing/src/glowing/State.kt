package glowing

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER
import org.lwjgl.opengl.GL15.glBindBuffer
import org.lwjgl.opengl.GL30.glBindVertexArray

import spatial.Vector4

class State {

  var clearColor: Vector4 = Vector4(0f, 0f, 0f, 1f)
    set(value) {
      if (field != value) {
        field = value
        glClearColor(value.r, value.g, value.b, value.a)
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

}

val globalState = State()