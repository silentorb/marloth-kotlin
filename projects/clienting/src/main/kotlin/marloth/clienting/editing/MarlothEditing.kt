package marloth.clienting.editing

import marloth.clienting.input.GuiCommandType
import marloth.definition.misc.loadMarlothGraphLibrary
import marloth.scenery.enums.MeshId
import marloth.scenery.enums.TextureId
import silentorb.mythic.editing.*
import silentorb.mythic.editing.panels.defaultViewportId
import silentorb.mythic.ent.reflectProperties
import silentorb.mythic.happenings.Commands
import silentorb.mythic.haft.InputDeviceState
import silentorb.mythic.spatial.Vector3

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
        textures = reflectProperties<String>(TextureId).map { Option(it, it) },
        meshes = reflectProperties<String>(MeshId).map { Option(it, it) },
        state = EditorState(
            graphLibrary = loadMarlothGraphLibrary(),
            graph = "root",
            cameras = mapOf(defaultViewportId to CameraRig(location = Vector3(-10f, 0f, 0f))),
        ),
    )

fun updateEditor(deviceStates: List<InputDeviceState>, previous: Editor): EditorState {
  val commands = mapCommands(defaultEditorBindings(), deviceStates)
  val cameras = previous.state.cameras
      .mapValues { (_, camera) ->
        updateFlyThroughCamera(commands, camera)
      }
  return previous.state.copy(
      cameras = cameras
  )
}

fun updateEditingActive(commands: Commands, previousIsActive: Boolean): Boolean =
    if (commands.any { it.type == GuiCommandType.editor })
      !previousIsActive
    else
      previousIsActive

fun updateEditing(deviceStates: List<InputDeviceState>, isActive: Boolean, previous: Editor?): EditorState? =
    if (isActive)
      updateEditor(deviceStates, previous ?: newEditor())
    else
      previous?.state
