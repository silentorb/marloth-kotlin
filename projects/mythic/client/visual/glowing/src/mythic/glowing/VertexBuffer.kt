package mythic.glowing

import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL30.glDeleteVertexArrays
import java.nio.FloatBuffer

class VertexBuffer(vertexSchema: VertexSchema) {
  private val vbo = glGenBuffers()
  private val vao: VertexArrayObject
  private val disposed = false

  init {
    globalState.vertexBufferObject = vbo
    vao = VertexArrayObject(vertexSchema)
    checkError("binding vbo buffer data")
  }

  fun load(vertices: FloatBuffer){
    globalState.vertexBufferObject = vbo
    glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)
  }

  fun activate() {
    globalState.vertexArrayObject = vao.id
  }

  fun dispose() {
    if (disposed)
      return

    // There seems to be a multi-platform hardware bug where deleting the currently bound
    // vbo and then generating a new one results in corrupting the new vbo.
    if (globalState.vertexBufferObject == vbo)
      globalState.vertexBufferObject = 0

    if (globalState.vertexArrayObject == vao.id)
      globalState.vertexArrayObject = 0

    glDeleteVertexArrays(vao.id)
    glDeleteBuffers(vbo)
  }
}