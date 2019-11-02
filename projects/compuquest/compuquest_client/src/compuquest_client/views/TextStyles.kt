package compuquest_client.views

import mythic.drawing.grayTone
import mythic.typography.IndexedTextStyle

val mainFontSize = 36
val smallFontSize = 22
private val grayColor = grayTone(0.7f)

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
