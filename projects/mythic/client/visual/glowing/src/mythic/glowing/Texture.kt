package mythic.glowing

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.*
import java.nio.FloatBuffer

typealias TextureInitializer = (width: Int, height: Int, buffer: FloatBuffer) -> Unit
typealias SimpleTextureInitializer = (width: Int, height: Int) -> Unit

val geometryTextureInitializer = { width: Int, height: Int, buffer: FloatBuffer ->
  glPixelStorei(GL_UNPACK_ALIGNMENT, 1) // Disable byte-alignment restriction
  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

  glTexImage2D(
      GL_TEXTURE_2D, 0, GL_RGB,
      width,
      height,
      0, GL_RGB, GL_FLOAT, buffer)
}

class Texture(width: Int, height: Int) {
  val id: Int = glGenTextures()

  init {
    globalState.boundTexture = id
  }

  constructor(width: Int, height: Int, buffer: FloatBuffer, initializer: TextureInitializer) : this(width, height) {
    initializer(width, height, buffer)
  }

  constructor(width: Int, height: Int, initializer: SimpleTextureInitializer) : this(width, height) {
    initializer(width, height)
  }
  fun dispose() {
    glDeleteTextures(id)
  }

  fun activate() {
    globalState.textureSlot = GL_TEXTURE0
    globalState.boundTexture = id
  }
}