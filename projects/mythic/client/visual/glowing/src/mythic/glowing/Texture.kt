package mythic.glowing

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.*
import java.nio.FloatBuffer

typealias TextureInitializer = (width: Int, height: Int, buffer: FloatBuffer) -> Unit
typealias SimpleTextureInitializer = (width: Int, height: Int) -> Unit

data class TextureAttributes(
    val repeating: Boolean = true
)

fun geometryTextureInitializer(attributes: TextureAttributes) = { width: Int, height: Int, buffer: FloatBuffer ->
  glPixelStorei(GL_UNPACK_ALIGNMENT, 1) // Disable byte-alignment restriction
  val wrapMode = if (attributes.repeating)
    GL_REPEAT
  else
    GL_CLAMP

  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapMode)
  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapMode)
  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

  glTexImage2D(
      GL_TEXTURE_2D, 0, GL_RGB,
      width,
      height,
      0, GL_RGB, GL_FLOAT, buffer)
}

enum class TextureTarget {
  general,
  multisample
}

class Texture(width: Int, height: Int, val target: TextureTarget) {
  var id: Int = glGenTextures()

  init {
    if (target == TextureTarget.multisample) {
      globalState.multisampleEnabled = true
    }

    bind()
  }

  constructor(width: Int, height: Int, buffer: FloatBuffer, initializer: TextureInitializer, target: TextureTarget = TextureTarget.general) : this(width, height, target) {
    initializer(width, height, buffer)
  }

  constructor(width: Int, height: Int, initializer: SimpleTextureInitializer, target: TextureTarget = TextureTarget.general) : this(width, height, target) {
    initializer(width, height)
  }

  constructor(width: Int, height: Int, buffer: FloatBuffer, attributes: TextureAttributes, target: TextureTarget = TextureTarget.general) : this(width, height, target) {
    geometryTextureInitializer(attributes)(width, height, buffer)
  }

  fun dispose() {
    glDeleteTextures(id)
    id = 0
  }

  private fun bind() {
    when (target) {
      TextureTarget.general -> globalState.bound2dTexture = id
      TextureTarget.multisample -> globalState.bound2dMultisampleTexture = id
    }
  }

  fun activate() {
    globalState.textureSlot = GL_TEXTURE0
    bind()
  }
}