package marloth.clienting.editing

import marloth.clienting.ClientEvent
import marloth.clienting.EditorState
import marloth.clienting.gui.EventUnion
import marloth.clienting.input.GuiCommandType
import silentorb.mythic.editing.*
import silentorb.mythic.haft.HaftCommands

val fonts = listOf(
    Typeface(
        name = "EBGaramond",
        path = "fonts/EBGaramond-Regular.ttf",
        size = 16f
    )
)

fun prepareEditorGui(window: Long, editorState: EditorState): EditorResult? {
  return if (editorState.isActive) {
    ensureImGuiIsInitialized(fonts, window)
    defineEditorGui()
  } else
    null
}

fun renderEditorGui(editorState: EditorState) {
  if (editorState.isActive) {
    renderEditorGui()
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
