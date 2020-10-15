package marloth.clienting.editing

import marloth.clienting.ClientEvent
import marloth.clienting.EditorState
import marloth.clienting.gui.EventUnion
import marloth.clienting.input.GuiCommandType
import silentorb.mythic.editing.Typeface
import silentorb.mythic.editing.ensureImGuiIsInitialized
import silentorb.mythic.editing.updateEditorGui
import silentorb.mythic.haft.HaftCommands

val fonts = listOf(
    Typeface(
        name = "EBGaramond",
        path = "fonts/EBGaramond-Regular.ttf",
        size = 16f
    )
)

fun updateMarlothEditor(window: Long, editorState: EditorState) {
  if (editorState.isActive) {
    ensureImGuiIsInitialized(fonts, window)
    updateEditorGui()
  }
}

fun updateEditorState(commands: HaftCommands, previous: EditorState): EditorState {
  return previous.copy(
      isActive = if (commands.any { it.type == GuiCommandType.editor })
        !previous.isActive
      else
        previous.isActive
  )
}
