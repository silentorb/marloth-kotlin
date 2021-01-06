package marloth.clienting

import marloth.clienting.audio.AudioConfig
import marloth.clienting.editing.newEditor
import marloth.clienting.gui.GuiState
import marloth.clienting.input.InputState
import marloth.clienting.gui.ViewId
import marloth.clienting.input.GameInputConfig
import marloth.clienting.input.newInputState
import marloth.scenery.enums.MeshShapeMap
import marloth.scenery.enums.TextResourceMapper
import silentorb.mythic.aura.AudioState
import silentorb.mythic.aura.newAudioState
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.editing.Editor
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Command
import silentorb.mythic.platforming.DisplayMode

const val canvasRendererKey = "renderer"

typealias PlayerViews = Map<Id, ViewId?>

fun initialEditor(textLibrary: TextResourceMapper, meshShapes: MeshShapeMap): Editor? =
    if (getDebugBoolean("START_EDITOR"))
      newEditor(textLibrary, meshShapes)
    else
      null

data class ClientState(
    val audio: AudioState,
    val guiStates: Map<Id, GuiState>,
    val commands: List<Command>,
    val input: InputState,
    val events: List<Any>,
    val displayModes: List<DisplayMode>,
    val editor: Editor? = null,
    val isEditorActive: Boolean = getDebugBoolean("START_EDITOR"),

    // Player ids could be purely maintained in the world deck except the world does not care about player order.
    // Player order is only a client concern, and only for local multiplayer.
    // The only reason for this players list is to keep track of the client player order.
    val players: List<Id>
)

fun newClientState(
    textLibrary: TextResourceMapper,
    inputConfig: GameInputConfig,
    audioConfig: AudioConfig,
    displayModes: List<DisplayMode>,
    meshShapes: MeshShapeMap,
) =
    ClientState(
        input = newInputState(inputConfig),
        guiStates = mapOf(),
        audio = newAudioState(audioConfig.soundVolume),
        commands = listOf(),
        players = listOf(),
        events = listOf(),
        displayModes = displayModes,
        editor = initialEditor(textLibrary, meshShapes)
    )
