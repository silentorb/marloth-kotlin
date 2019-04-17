package marloth.clienting.gui

import mythic.drawing.grayTone
import mythic.spatial.Vector4
import mythic.typography.IndexedTextStyle
import kotlin.reflect.full.memberProperties

private val black = Vector4(0f, 0f, 0f, 1f)
private val white = Vector4(1f, 1f, 1f, 1f)
private val grayColor = grayTone(0.7f)

class TextStyles {
  companion object {

    val smallBlack = IndexedTextStyle(
        0,
        12,
        color = black
    )

    val smallWhite = IndexedTextStyle(
        0,
        12,
        color = white
    )

    val gray = IndexedTextStyle(
        0,
        12,
        color = grayColor
    )
  }
}

fun enumerateTextStyles(styles: Any): List<IndexedTextStyle> {
  return styles.javaClass.kotlin.memberProperties.map { member ->
    member.get(styles) as IndexedTextStyle
  }
}