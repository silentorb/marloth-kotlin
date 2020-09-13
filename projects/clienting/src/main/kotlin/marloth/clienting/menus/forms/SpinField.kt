package marloth.clienting.menus.forms

import marloth.clienting.ClientEvent
import marloth.clienting.ClientEventType
import marloth.clienting.menus.OnClientEvents
import marloth.clienting.menus.logic.cycle
import marloth.clienting.menus.TextStyles
import marloth.clienting.menus.logic.onActivateKey
import marloth.clienting.menus.logic.onClickKey
import marloth.clienting.menus.logic.onClientEventsKey
import marloth.scenery.enums.CharacterRigCommands
import silentorb.mythic.bloom.*

const val previousOptionKey = "silentorb.bloom.previousOption"
const val nextOptionKey = "silentorb.bloom.nextOption"

data class Option(
    val label: String,
    val id: Any
)

fun spinButton(text: String, attributes: Map<String, Any?>): Box =
    label(TextStyles.mediumBlack, text)
        .copy(attributes = attributes)

fun <T> cycle(options: List<T>, offset: Int, value: T): T {
  assert(options.any())
  val index = options.indexOf(value)
  assert(index != -1)
  val nextIndex = cycle(index + offset, options.size)
  return options[nextIndex]
}

fun <T> spinField(options: List<T>, id: Any, valueText: String): Box {
  val decrementEvent = ClientEvent(ClientEventType.setWindowMode, cycle(options, 1, id))
  val incrementEvent = ClientEvent(ClientEventType.setWindowMode, cycle(options, -1, id))

  return horizontalList(spacing = 10)(
      listOf(
          spinButton("<", mapOf(previousOptionKey to id, onClickKey to decrementEvent)),
          label(TextStyles.mediumBlack, valueText).addAttributes(onClickKey to incrementEvent),
          spinButton(">", mapOf(nextOptionKey to id, onClickKey to incrementEvent))
      )
  )
      .addAttributes(
          onActivateKey to incrementEvent,
          onClientEventsKey to OnClientEvents(
              listOf(
                  decrementEvent to { it.type == CharacterRigCommands.moveLeft },
                  incrementEvent to { it.type == CharacterRigCommands.moveRight }
              )
          )
      )
}

//fun <T> updateSpinField(options: List<T>, commands: List<Any>, hoverBoxes: Collection<OffsetBox>, value: T): T =
//    when {
//      commands.contains(CharacterRigCommands.moveRight) ||
//          commands.contains(GuiCommandType.menuSelect) -> cycle(options, 1, value)
//      commands.contains(CharacterRigCommands.moveLeft) -> cycle(options, -1, value)
//      else -> value
//    }
