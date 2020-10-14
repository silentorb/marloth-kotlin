package marloth.integration.editing

import silentorb.mythic.editing.Typeface
import silentorb.mythic.editing.ensureImGuiIsInitialized
import silentorb.mythic.editing.updateEditorGui

val fonts = listOf(
    Typeface(
        name = "EBGaramond",
        path = "fonts/EBGaramond-Regular.ttf",
        size = 16f
    )
)

fun updateMarlothEditor(window: Long) {
  ensureImGuiIsInitialized(fonts, window)
  updateEditorGui()
}
