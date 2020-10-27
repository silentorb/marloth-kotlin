package marloth.clienting.editing

import marloth.clienting.input.GuiCommandType
import marloth.definition.misc.loadMarlothGraphLibrary
import silentorb.mythic.editing.*
import silentorb.mythic.haft.HaftCommands

private var graphLibrary: GraphLibrary? = null

val editorFonts = listOf(
    Typeface(
        name = "EBGaramond",
        path = "fonts/EBGaramond-Regular.ttf",
        size = 16f
    )
)

fun newEditor() =
    Editor(
        propertyDefinitions = commonPropertyDefinitions(),
        graphLibrary = loadMarlothGraphLibrary(),
        graph = "root"
    )

fun updateEditor(commands: HaftCommands, previous: Editor): Editor {
//  return updateFlyThroughCamera(commands, listOf(), previous.cameras[])
  return previous
}

fun updateEditingActive(commands: HaftCommands, previousIsActive: Boolean): Boolean =
    if (commands.any { it.type == GuiCommandType.editor })
      !previousIsActive
    else
      previousIsActive

fun updateEditing(commands: HaftCommands, isActive: Boolean, previous: Editor?): Editor? {
  val editor = if (isActive)
    previous ?: newEditor()
  else
    null

  return if (isActive && editor != null)
    updateEditor(commands, editor)
  else
    null
}
