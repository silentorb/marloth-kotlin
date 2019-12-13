package lab.views.shared

import marloth.clienting.gui.smallFontSize
import silentorb.mythic.spatial.Vector4
import silentorb.mythic.typography.IndexedTextStyle

private val lessRedColor = Vector4(0.5f, 1f, 1f, 1f)

class LabTextStyles {
  companion object {

    val lessRed = IndexedTextStyle(
        0,
        smallFontSize,
        color = lessRedColor
    )

  }
}
