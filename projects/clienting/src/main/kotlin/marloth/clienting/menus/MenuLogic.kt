package marloth.clienting.menus

import marloth.clienting.MarlothBloomState
import marloth.clienting.input.GuiCommandType
import marloth.scenery.enums.CharacterRigCommands
import silentorb.mythic.bloom.next.Box
import silentorb.mythic.ent.firstNotNull
import silentorb.mythic.haft.HaftCommand
import silentorb.mythic.haft.HaftCommands

const val menuKey = "silentorb.menu"
const val menuItemIndexKey = "silentorb.menuItem"

fun cycle(value: Int, max: Int): Int {
  assert(max > 0)
  return (value + max) % max
}

data class MenuLayer(
    val view: ViewId,
    val focusIndex: Int
)

typealias MenuStack = List<MenuLayer>

fun getMenuEvents(menu: Menu?, index: Int, events: HaftCommands): List<ClientOrServerEvent> =
    if (menu == null)
      listOf()
    else {
      val activated = events.any { it.type == GuiCommandType.menuSelect }
      val menuItem = menu[index]
      if (activated)
        listOfNotNull(menuItem.event)
      else
        listOf()
    }

fun getHoverIndex(hoverBoxes: List<Box>): Int? =
    hoverBoxes
        .firstNotNull { it.attributes[menuItemIndexKey] as? Int }

fun updateMenuFocus(stack: MenuStack, menuSize: Int, command: HaftCommand?, index: Int) =
    when (command?.type) {
      CharacterRigCommands.moveDown -> cycle(index + 1, menuSize)
      CharacterRigCommands.moveUp -> cycle(index - 1, menuSize)
      GuiCommandType.navigate -> 0
      GuiCommandType.menuBack -> stack.lastOrNull()?.focusIndex ?: 0
      else -> index
    }

fun updateMenuStack(command: HaftCommand, state: MarlothBloomState): MenuStack {
  val stack = state.menuStack
  return when (command.type) {
    GuiCommandType.navigate -> stack + MenuLayer(state.view!!, state.menuFocusIndex)
    GuiCommandType.menuBack -> stack.dropLast(1)
    GuiCommandType.menu -> listOf()
    else -> stack
  }
}
