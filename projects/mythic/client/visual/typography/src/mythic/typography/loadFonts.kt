package mythic.typography

import org.lwjgl.BufferUtils
import org.lwjgl.system.jni.JNINativeInterface.GetDirectBufferAddress

fun loadCharacters(face: Long, dimensions: IntegerVector2): CharacterMap {
  val characters: MutableMap<Char, Glyph> = mutableMapOf()
  var verticalOffset = 0f
  for (value in CharRange('!', '~')) {
    val info = FaceLoader.loadCharacterInfo(face, value)
    characters[value] = Glyph(info,
        verticalOffset / dimensions.y,
        info.sizeY.toFloat() / dimensions.y
    )
    verticalOffset += info.sizeY + 2
  }

  return characters
}

fun loadFont(freetype: Long, info: FontLoadInfo): Font {
  val face = FaceLoader.loadFace(freetype, info.filename, info.pixelHeight)
  val dimensions = FaceLoader.getTextureDimensions(face)
  val buffer = BufferUtils.createByteBuffer(dimensions.x * dimensions.y)
  val characters = loadCharacters(face, dimensions)
  FaceLoader.renderFaces(freetype, face, GetDirectBufferAddress(buffer), dimensions.x)
  FaceLoader.releaseFace(face)

  val texture = generateFontTexture(buffer, dimensions.x, dimensions.y)
  val font = Font(characters, texture, dimensions, info.defaultSpacing)

  return font
}

data class FontLoadInfo(
    val filename: String,
    val pixelHeight: Int,
    val defaultSpacing: Float
)

fun loadFonts(files: List<FontLoadInfo>): List<Font> {
  val freetype = FaceLoader.initializeFreetype()
  try {
    return files.map { loadFont(freetype, it) }
  } finally {
    FaceLoader.releaseFreetype(freetype)
  }
}