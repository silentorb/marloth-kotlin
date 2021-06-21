package marloth.clienting.gui

import marloth.clienting.gui.menus.logic.DisplayChangeState
import marloth.clienting.gui.menus.logic.MenuStack
import silentorb.mythic.bloom.BloomState
import silentorb.mythic.bloom.Box
import silentorb.mythic.bloom.Flower
import silentorb.mythic.ent.Id
import simulation.happenings.Notification
import simulation.misc.Definitions

enum class DeviceMode {
  mouseKeyboard,
  gamepad
}

data class GuiState(
    val menuStack: MenuStack,
    val view: ViewId?,
    val menuFocusIndex: Int,
    val displayChange: DisplayChangeState? = null,
    val primarydeviceMode: DeviceMode,
    val notifications: List<Notification> = listOf(),
    val bloomState: BloomState = mapOf(),
)

typealias GuiStateMap = Map<Id, GuiState>
typealias StateFlower = (Definitions, GuiState) -> Box
typealias StateFlowerTransform = (Definitions, GuiState) -> Flower
