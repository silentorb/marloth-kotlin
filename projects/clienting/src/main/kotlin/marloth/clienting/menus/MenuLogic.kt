package marloth.clienting.menus

import marloth.clienting.MarlothBloomState
import marloth.clienting.input.GuiCommandType
import marloth.scenery.enums.CharacterRigCommands
import silentorb.mythic.bloom.Box
import silentorb.mythic.ent.firstNotNull
import silentorb.mythic.haft.HaftCommands

const val menuKey = "silentorb.menu"
const val onActivateKey = "silentorb.onActivate"
const val onClickKey = "silentorb.onClick"
const val menuItemIndexKey = "silentorb.menuItemIndex"

fun cycle(value: Int, max: Int): Int {
  assert(max > 0)
  return (value + max) % max
}

data class MenuLayer(
    val view: ViewId,
    val focusIndex: Int
)

typealias MenuStack = List<MenuLayer>

fun getMenuEvents(attributeBoxes: List<Box>, hoverBoxes: List<Box>, events: HaftCommands): List<ClientOrServerEvent> {
  // The pattern between these two blocks could be abstracted but it may be an accidental pattern
  // so I'll wait until another repetition of the pattern occurs
  val clickEvents = if (events.any { it.type == GuiCommandType.mouseClick })
    hoverBoxes.mapNotNull { it.attributes[onClickKey] as? ClientOrServerEvent }
  else
    listOf()

  val menuSelectEvents = if (events.any { it.type == GuiCommandType.menuSelect })
    attributeBoxes.mapNotNull { it.attributes[onActivateKey] as? ClientOrServerEvent }
  else
    listOf()

  return clickEvents + menuSelectEvents
}

fun getHoverIndex(hoverBoxes: List<Box>): Int? =
    hoverBoxes
        .firstNotNull { it.attributes[menuItemIndexKey] as? Int }

fun updateMenuFocus(stack: MenuStack, menuSize: Int, commands: List<Any>, hoverFocusIndex: Int?, index: Int) =
    when {
      commands.contains(CharacterRigCommands.moveDown) -> cycle(index + 1, menuSize)
      commands.contains(CharacterRigCommands.moveUp) -> cycle(index - 1, menuSize)
      commands.contains(GuiCommandType.navigate) -> 0
      commands.contains(GuiCommandType.menuBack) -> stack.lastOrNull()?.focusIndex ?: 0
      else -> hoverFocusIndex ?: index
    }

fun updateMenuStack(commands: List<Any>, state: MarlothBloomState): MenuStack {
  val stack = state.menuStack
  return when {
    commands.contains(GuiCommandType.navigate) -> stack + MenuLayer(state.view!!, state.menuFocusIndex)
    commands.contains(GuiCommandType.menuBack) -> stack.dropLast(1)
    commands.contains(GuiCommandType.menu) -> listOf()
    else -> stack
  }
}
