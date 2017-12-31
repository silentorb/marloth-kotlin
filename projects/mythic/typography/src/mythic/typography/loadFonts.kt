package mythic.typography

import org.lwjgl.BufferUtils
import org.lwjgl.system.jni.JNINativeInterface.GetDirectBufferAddress

fun loadCharacters(face: Long, dimensions: IntegerVector2): CharacterMap {
  val characters: MutableMap<Char, Glyph> = mutableMapOf()
  var verticalOffset = 0
  for (value in CharRange('!', '~')) {
    val info = FaceLoader.loadCharacterInfo(face, value)
    characters[value] = Glyph(info,
        verticalOffset.toFloat() / dimensions.y,
        info.sizeY.toFloat() / dimensions.y
    )
  }

  return characters
}

fun loadFont(freetype: Long, filename: String): Font {
  val face = FaceLoader.loadFace(freetype, filename)
  val dimensions = FaceLoader.getTextureDimensions(face)
  val buffer = BufferUtils.createByteBuffer(dimensions.x * dimensions.y)
  val characters = loadCharacters(face, dimensions)
  FaceLoader.renderFaces(freetype, face, GetDirectBufferAddress(buffer), dimensions.x)
  FaceLoader.releaseFace(face)

  val texture = generateFontTexture(buffer, dimensions.x, dimensions.y)
  val font = Font(characters, texture)
  return font
}

fun loadFonts(files: List<String>): List<Font> {
  val freetype = FaceLoader.initializeFreetype()
  try {
    return files.map { loadFont(freetype, it) }
  } finally {
    FaceLoader.releaseFreetype(freetype)
  }
}