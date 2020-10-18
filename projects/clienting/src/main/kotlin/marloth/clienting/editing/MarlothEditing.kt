package marloth.clienting.editing

import marloth.clienting.input.GuiCommandType
import silentorb.mythic.editing.*
import silentorb.mythic.haft.HaftCommands

val editorFonts = listOf(
    Typeface(
        name = "EBGaramond",
        path = "fonts/EBGaramond-Regular.ttf",
        size = 16f
    )
)

fun renderEditorGui(editor: Editor?) {
  if (isActive(editor)) {
    renderEditorGui()
  }
}

fun updateEditorState(commands: HaftCommands, previous: Editor?): Editor? {
  val previousIsActive = previous?.isActive ?: false
  val active = if (commands.any { it.type == GuiCommandType.editor })
    !previousIsActive
  else
    previousIsActive

  return if (active) {
    (previous ?: Editor()).copy(
        isActive = active
    )
  } else
    previous
}
