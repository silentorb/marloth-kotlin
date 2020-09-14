package marloth.clienting.gui.menus.forms

import marloth.clienting.ClientEvent
import marloth.clienting.ClientEventType
import marloth.clienting.gui.OnClientEvents
import marloth.clienting.gui.menus.logic.cycle
import marloth.clienting.gui.menus.TextStyles
import marloth.clienting.gui.menus.logic.onActivateKey
import marloth.clienting.gui.menus.logic.onClickKey
import marloth.clienting.gui.menus.logic.onClientEventsKey
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
