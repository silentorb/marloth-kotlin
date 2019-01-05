package mythic.glowing

import org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT
import org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.*
import org.lwjgl.opengl.GL30.glGenerateMipmap
import java.nio.ByteBuffer
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
    val storageUnit: TextureStorageUnit,
    val mipmap: Boolean = false
)

private var _maxAnistropy: Float? = null

fun getMaxAnistropy(): Float {
  if (_maxAnistropy != null)
    return _maxAnistropy!!

  _maxAnistropy = glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT)
  return _maxAnistropy!!
}

fun initializeTexture(width: Int, height: Int, attributes: TextureAttributes, buffer: ByteBuffer? = null) {
  glPixelStorei(GL_UNPACK_ALIGNMENT, 1) // Disable byte-alignment restriction
  val wrapMode = if (attributes.repeating)
    GL_REPEAT
  else
    GL_CLAMP

  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapMode)
  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapMode)

  val (minFilter, magFilter) = if (attributes.mipmap)
    Pair(GL_NEAREST_MIPMAP_NEAREST, GL_NEAREST)
//  Pair(GL_LINEAR_MIPMAP_LINEAR, GL_NEAREST)
  else if (attributes.smooth)
    Pair(GL_LINEAR, GL_LINEAR)
  else
    Pair(GL_NEAREST, GL_NEAREST)

  if (attributes.smooth || attributes.smooth) {
    val maxAnistropy = getMaxAnistropy()
    if (maxAnistropy != 0f) {
      glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAnistropy)
    }
  }

  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter)
  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter)

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

  if (attributes.mipmap) {
    glGenerateMipmap(GL_TEXTURE_2D)
  }
  checkError("Initializing texture")
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

  constructor(width: Int, height: Int, attributes: TextureAttributes, buffer: ByteBuffer? = null, target: TextureTarget = TextureTarget.general) : this(width, height, target) {
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