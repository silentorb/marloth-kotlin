package mythic.typography

data class IntegerVector2(val x: Int, val y: Int)

data class GlyphInfo(
    val sizeX: Int,
    val sizeY: Int,
    val bearingX: Int,
    val bearingY: Int,
    val advance: Int
)

data class Glyph(
    val info: GlyphInfo,
    val offset: Float,
    val height: Float
)

//data class GlyphInfo(
//    val sizeX: Int,
//    val sizeY: Int,
//    val bearingX: Int,
//    val bearingY: Int,
//    val advance: Int,
//    val offset: Float,
//    val height: Float
//)

class FaceLoader {

  companion object {
    init {
      System.loadLibrary("libjava_freetype")
    }

    @JvmStatic
    external fun getTextureDimensions(face: Long): IntegerVector2

    @JvmStatic
    external fun loadFace(freetype: Long, filename: String): Long

    @JvmStatic
    external fun loadCharacterInfo(face: Long, value: Char): GlyphInfo

    @JvmStatic
    external fun renderFaces(freetype: Long, face: Long, buffer: Long, width: Int)

    @JvmStatic
    external fun initializeFreetype(): Long

    @JvmStatic
    external fun releaseFace(face: Long)

    @JvmStatic
    external fun releaseFreetype(freetype: Long)
  }

}