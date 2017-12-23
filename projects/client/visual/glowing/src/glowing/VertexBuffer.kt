package glowing

import org.lwjgl.opengl.GL15.*
import java.nio.FloatBuffer

class VertexBuffer(buffer: FloatBuffer) {
  val vbo = glGenBuffers()

  init {
    glBindBuffer(GL_ARRAY_BUFFER, vbo)
    glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW)
  }

  fun activate() {
    globalState.vertexBufferObject = vbo
  }
}