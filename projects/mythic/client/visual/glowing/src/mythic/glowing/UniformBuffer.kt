package mythic.glowing

import org.lwjgl.opengl.ARBUniformBufferObject.GL_UNIFORM_BUFFER
import org.lwjgl.opengl.GL15.*
import java.nio.ByteBuffer

class UniformBuffer() {
  private val bufferObject = glGenBuffers()
  private val disposed = false

  fun load(data: ByteBuffer) {
    globalState.uniformBufferObject = bufferObject
    glBufferData(GL_UNIFORM_BUFFER, data, GL_DYNAMIC_DRAW)
    globalState.uniformBufferObject = 0
  }

  fun activate() {
    globalState.uniformBufferObject = bufferObject
  }

  fun dispose() {
    if (disposed)
      return

    if (globalState.uniformBufferObject == bufferObject)
      globalState.uniformBufferObject = 0

    glDeleteBuffers(bufferObject)
  }
}