package marloth.clienting.menus

import silentorb.mythic.drawing.grayTone
import silentorb.mythic.spatial.Vector4
import silentorb.mythic.typography.FontLoadInfo
import silentorb.mythic.typography.IndexedTextStyle

val black = Vector4(0f, 0f, 0f, 1f)
private val white = Vector4(1f, 1f, 1f, 1f)
private val grayColor = grayTone(0.7f)

val baseFonts = listOf(
    FontLoadInfo(
        filename = "fonts/EBGaramond-Regular.ttf",
        pixelHeight = 0
    )
)

val mainFontSize = 36

val smallFontSize = 22

class textStyles {
  companion object {

    val smallBlack = IndexedTextStyle(
        0,
        smallFontSize,
        color = black
    )

    val smallWhite = IndexedTextStyle(
        0,
        smallFontSize,
        color = white
    )

    val mediumBlack = IndexedTextStyle(
        0,
        mainFontSize,
        color = black
    )

    val mediumWhite = IndexedTextStyle(
        0,
        mainFontSize,
        color = white
    )

    val gray = IndexedTextStyle(
        0,
        mainFontSize,
        color = grayColor
    )

    val smallGray = IndexedTextStyle(
        0,
        smallFontSize,
        color = grayColor
    )
  }
}