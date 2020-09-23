package marloth.clienting

import marloth.clienting.input.InputState
import marloth.clienting.gui.ViewId
import marloth.clienting.gui.menus.logic.DisplayChangeState
import marloth.clienting.gui.menus.logic.MenuStack
import marloth.clienting.rendering.marching.MarchingState
import silentorb.mythic.aura.AudioState
import silentorb.mythic.bloom.BloomState
import silentorb.mythic.bloom.Box
import silentorb.mythic.ent.Id
import silentorb.mythic.haft.HaftCommand
import silentorb.mythic.lookinglass.DisplayOptions
import silentorb.mythic.platforming.DisplayMode
import simulation.misc.Definitions

const val canvasRendererKey = "renderer"

typealias PlayerViews = Map<Id, ViewId?>

data class GuiState(
    val bloom: BloomState,
    val menuStack: MenuStack,
    val view: ViewId?,
    val menuFocusIndex: Int,
    val displayChange: DisplayChangeState? = null
)

typealias GuiStateMap = Map<Id, GuiState>
typealias StateFlower = (Definitions, GuiState) -> Box

data class ClientState(
    val audio: AudioState,
    val guiStates: Map<Id, GuiState>,
    val commands: List<HaftCommand>,
    val input: InputState,
    val marching: MarchingState,
    val events: List<Any>,
    val displayModes: List<DisplayMode>,

    // Player ids could be purely maintained in the world deck except the world does not care about player order.
    // Player order is only a client concern, and only for local multiplayer.
    // The only reason for this players list is to keep track of the client player order.
    val players: List<Id>
)
