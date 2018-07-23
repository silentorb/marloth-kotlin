package mythic.glowing

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.*
import java.nio.FloatBuffer

typealias TextureInitializer = (width: Int, height: Int, buffer: FloatBuffer?) -> Unit

enum class TextureFormat {
  rgb,
  depth
}

enum class TextureStorageUnit {
  float,
  unsigned_byte
}


data class TextureAttributes(
    val repeating: Boolean = true,
    val smooth: Boolean = true,
    val format: TextureFormat = TextureFormat.rgb,
    val storageUnit: TextureStorageUnit = TextureStorageUnit.float
)

fun initializeTexture(width: Int, height: Int, attributes: TextureAttributes, buffer: FloatBuffer? = null) {
  glPixelStorei(GL_UNPACK_ALIGNMENT, 1) // Disable byte-alignment restriction
  val wrapMode = if (attributes.repeating)
    GL_REPEAT
  else
    GL_CLAMP

  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapMode)
  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapMode)

  val filter = if (attributes.smooth)
    GL_LINEAR
  else
    GL_NEAREST

  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter)
  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter)

  val internalFormat = when (attributes.format) {
    TextureFormat.rgb -> GL_RGB
    TextureFormat.depth -> GL_DEPTH_COMPONENT
  }

  val storageUnit = when (attributes.storageUnit) {
    TextureStorageUnit.float -> GL_FLOAT
    TextureStorageUnit.unsigned_byte -> GL_UNSIGNED_BYTE
  }

  glTexImage2D(
      GL_TEXTURE_2D, 0, internalFormat,
      width,
      height,
      0, internalFormat, storageUnit, buffer)
}

enum class TextureTarget {
  general,
  multisample
}

class Texture(val width: Int, val height: Int, val target: TextureTarget) {
  var id: Int = glGenTextures()

  init {
    if (target == TextureTarget.multisample) {
      globalState.multisampleEnabled = true
    }

    bind()
  }

  constructor(width: Int, height: Int, buffer: FloatBuffer?, initializer: TextureInitializer, target: TextureTarget = TextureTarget.general) : this(width, height, target) {
    initializer(width, height, buffer)
  }

  constructor(width: Int, height: Int, attributes: TextureAttributes, buffer: FloatBuffer? = null, target: TextureTarget = TextureTarget.general) : this(width, height, target) {
    initializeTexture(width, height, attributes, buffer)
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

  fun activate(unit: Int = GL_TEXTURE0) {
    globalState.textureSlot = unit
    bind()
  }
}

fun activateTextures(textures: List<Texture>) {
  textures.forEachIndexed { index, texture ->
    texture.activate(GL_TEXTURE0 + index)
  }
}