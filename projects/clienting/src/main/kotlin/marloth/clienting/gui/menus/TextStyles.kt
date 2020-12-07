package marloth.clienting.gui.menus

import silentorb.mythic.drawing.Colors
import silentorb.mythic.drawing.grayTone
import silentorb.mythic.typography.FontLoadInfo
import silentorb.mythic.typography.IndexedTextStyle

val black = Colors.black
private val white = Colors.white
private val grayColor = grayTone(0.7f)

val baseFonts = listOf(
    FontLoadInfo(
        filename = "fonts/EBGaramond-Regular.ttf",
        pixelHeight = 0
    ),
    FontLoadInfo(
        filename = "fonts/EBGaramond-SemiBold.ttf",
        pixelHeight = 0
    ),
    FontLoadInfo(
        filename = "fonts/EBGaramond-Bold.ttf",
        pixelHeight = 0
    )
)

val mainFontSize = 36

val smallFontSize = 22

object TextStyles {
  val smallBlack = IndexedTextStyle(
      0,
      smallFontSize,
      color = black
  )

  val smallSemiBoldBlack = IndexedTextStyle(
      1,
      smallFontSize,
      color = black
  )

  val smallBoldBlack = IndexedTextStyle(
      2,
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
