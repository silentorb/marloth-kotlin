package marloth.clienting

import marloth.clienting.editing.newEditor
import marloth.clienting.input.InputState
import marloth.clienting.gui.ViewId
import marloth.clienting.gui.menus.logic.DisplayChangeState
import marloth.clienting.gui.menus.logic.MenuStack
import marloth.scenery.enums.TextResourceMapper
import silentorb.mythic.aura.AudioState
import silentorb.mythic.bloom.BloomState
import silentorb.mythic.bloom.Box
import silentorb.mythic.bloom.Flower
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.editing.Editor
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Command
import silentorb.mythic.platforming.DisplayMode
import simulation.misc.Definitions

const val canvasRendererKey = "renderer"

typealias PlayerViews = Map<Id, ViewId?>

enum class DeviceMode {
  mouseKeyboard,
  gamepad
}

data class GuiState(
    val bloom: BloomState,
    val menuStack: MenuStack,
    val view: ViewId?,
    val menuFocusIndex: Int,
    val displayChange: DisplayChangeState? = null,
    val primarydeviceMode: DeviceMode,
)

typealias GuiStateMap = Map<Id, GuiState>
typealias StateFlower = (Definitions, GuiState) -> Box
typealias StateFlowerTransform = (Definitions, GuiState) -> Flower

fun initialEditor(textLibrary: TextResourceMapper): Editor? =
    if (getDebugBoolean("START_EDITOR"))
      newEditor(textLibrary)
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
