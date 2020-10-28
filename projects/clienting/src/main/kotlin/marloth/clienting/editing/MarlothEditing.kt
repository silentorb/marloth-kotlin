package marloth.clienting.editing

import marloth.clienting.input.GuiCommandType
import marloth.definition.misc.loadMarlothGraphLibrary
import silentorb.mythic.editing.*
import silentorb.mythic.editing.panels.defaultViewportId
import silentorb.mythic.happenings.Commands
import silentorb.mythic.haft.InputDeviceState

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
        graph = "root",
        cameras = mapOf(defaultViewportId to CameraRig())
    )

fun updateEditor(deviceStates: List<InputDeviceState>, previous: Editor): Editor {
  val commands = mapCommands(defaultEditorBindings(), deviceStates)
  val cameras = previous.cameras
      .mapValues { (_, camera) ->
        updateFlyThroughCamera(commands, camera)
      }
  return previous.copy(
      cameras = cameras
  )
}

fun updateEditingActive(commands: Commands, previousIsActive: Boolean): Boolean =
    if (commands.any { it.type == GuiCommandType.editor })
      !previousIsActive
    else
      previousIsActive

fun updateEditing(deviceStates: List<InputDeviceState>, isActive: Boolean, previous: Editor?): Editor? =
    if (isActive)
      updateEditor(deviceStates, previous ?: newEditor())
    else
      previous
