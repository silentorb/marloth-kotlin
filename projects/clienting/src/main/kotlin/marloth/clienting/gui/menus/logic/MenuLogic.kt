package marloth.clienting.gui.menus.logic

import marloth.clienting.ClientEventType
import marloth.clienting.gui.GuiState
import marloth.clienting.input.GuiCommandType
import marloth.clienting.gui.EventUnion
import marloth.clienting.gui.OnClientEvents
import marloth.clienting.gui.ViewId
import marloth.scenery.enums.CharacterRigCommands
import silentorb.mythic.bloom.OffsetBox
import silentorb.mythic.bloom.menuItemIndexKey
import silentorb.mythic.ent.firstNotNull
import silentorb.mythic.haft.HaftCommand
import silentorb.mythic.happenings.Commands
import silentorb.mythic.happenings.handleCommands

const val menuLengthKey = "silentorb.menuLength"
const val onActivateKey = "silentorb.onActivate"
const val onClickKey = "silentorb.onClick"
const val onClientEventsKey = "silentorb.onClientEventKey"

fun cycle(value: Int, max: Int): Int {
  assert(max > 0)
  return (value + max) % max
}

fun <T> cycle(values: List<T>, value: T, offset: Int): T {
  assert(values.any())
  val index = cycle(values.indexOf(value) + offset, values.size)
  return values[index]
}

data class MenuLayer(
    val view: ViewId,
    val focusIndex: Int
)

typealias MenuStack = List<MenuLayer>

fun getMenuItemEvents(attributeBoxes: List<OffsetBox>, hoverBoxes: List<OffsetBox>, events: Commands): List<EventUnion> {
  // The pattern between these two blocks could be abstracted but it may be an accidental pattern
  // so I'll wait until another repetition of the pattern occurs
  val clickEvents = if (events.any { it.type == HaftCommand.leftMouseClick })
    hoverBoxes.mapNotNull {
      it.attributes[onClickKey] as List<EventUnion>?
    }
        .flatten()
  else
    listOf()

  val menuSelectEvents = if (events.any { it.type == GuiCommandType.menuSelect })
      attributeBoxes
        .mapNotNull { it.attributes[onActivateKey] }
        .filterIsInstance<List<Any>>()
        .flatten()
  else
    listOf()

  val customListenerEvents = attributeBoxes
      .mapNotNull { it.attributes[onClientEventsKey] }
      .filterIsInstance<OnClientEvents>()
      .flatMap { handlers ->
        handlers.map.mapNotNull { (event, pattern) ->
          if (events.any { pattern(it) })
            event
          else
            null
        }
      }

  return clickEvents + menuSelectEvents + customListenerEvents
}

fun getHoverIndex(hoverBoxes: List<OffsetBox>): Int? =
    hoverBoxes
        .firstNotNull { it.attributes[menuItemIndexKey] as? Int }

fun updateMenuFocus(stack: MenuStack, menuSize: Int, hoverFocusIndex: Int?) =
    handleCommands<Int> { command, index ->
      when (command.type) {
        CharacterRigCommands.moveBackward -> cycle(index + 1, menuSize)
        CharacterRigCommands.moveForward -> cycle(index - 1, menuSize)
        ClientEventType.navigate, ClientEventType.drillDown -> 0
        ClientEventType.menuBack ->
          stack.lastOrNull()?.focusIndex ?: 0
        else -> hoverFocusIndex ?: index
      }
    }

fun updateMenuStack(state: GuiState) =
    handleCommands<MenuStack> { command, index ->
      val stack = state.menuStack
      val view = state.view
      when {
        command.type == ClientEventType.drillDown && view != null -> stack + MenuLayer(view, state.menuFocusIndex)
        command.type == ClientEventType.menuBack -> stack.dropLast(1)
        command.type == GuiCommandType.menu -> listOf()
        else -> stack
      }
    }
