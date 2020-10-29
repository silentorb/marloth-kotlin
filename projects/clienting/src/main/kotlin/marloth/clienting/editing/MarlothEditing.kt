package marloth.clienting.editing

import marloth.clienting.input.GuiCommandType
import marloth.definition.misc.loadMarlothGraphLibrary
import marloth.scenery.enums.MeshId
import marloth.scenery.enums.TextureId
import silentorb.mythic.editing.*
import silentorb.mythic.editing.panels.defaultViewportId
import silentorb.mythic.ent.reflectProperties
import silentorb.mythic.haft.InputDeviceState
import silentorb.mythic.happenings.Commands
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
        graphLibrary = loadMarlothGraphLibrary(),
        state = loadEditorStateOrDefault(),
    )

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
