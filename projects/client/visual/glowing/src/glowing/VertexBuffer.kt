package glowing

import org.lwjgl.opengl.GL15.*
import java.nio.FloatBuffer

class VertexBuffer(vertices: FloatBuffer, vertexSchema: VertexSchema) {
  val vbo = glGenBuffers()
  val vao: VertexArrayObject

  init {
//    globalState.vertexArrayObject = 0
    globalState.vertexBufferObject = vbo
    glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)
    checkError("binding vbo buffer data")
    vao = VertexArrayObject(vertexSchema)
  }

  fun activate() {
    globalState.vertexArrayObject = vao.id
  }
}