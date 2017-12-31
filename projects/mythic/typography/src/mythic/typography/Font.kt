package mythic.typography

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE
import java.nio.ByteBuffer

typealias CharacterMap = Map<Char, Glyph>

fun generateFontTexture(buffer: ByteBuffer, width: Int, height: Int): Int {
  val texture = glGenTextures()
  glBindTexture(GL_TEXTURE_2D, texture)

  glPixelStorei(GL_UNPACK_ALIGNMENT, 1) // Disable byte-alignment restriction

  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

  glTexImage2D(
      GL_TEXTURE_2D, 0, GL_RED,
      width,
      height,
      0, GL_RED, GL_UNSIGNED_BYTE, buffer);

  return texture
}

data class Font(val characters: CharacterMap,
                val texture: Int) {

}