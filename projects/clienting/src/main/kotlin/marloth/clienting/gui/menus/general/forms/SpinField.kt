package marloth.clienting.gui.menus.general.forms

import marloth.clienting.ClientEvent
import marloth.clienting.ClientEventType
import marloth.clienting.gui.OnClientEvents
import marloth.clienting.gui.menus.general.MenuItemFlower
import marloth.clienting.gui.menus.logic.cycle
import marloth.clienting.gui.menus.TextStyles
import marloth.clienting.gui.menus.logic.onActivateKey
import marloth.clienting.gui.menus.logic.onClickKey
import marloth.clienting.gui.menus.logic.onClientEventsKey
import marloth.scenery.enums.CharacterRigCommands
import silentorb.mythic.bloom.*

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

data class SpinHandlers(
    val incrementEvents: List<Any>,
    val decrementEvents: List<Any>
)

fun spinField(valueText: String, handlers: SpinHandlers): MenuItemFlower = { hasFocus ->
  val incrementEvent = handlers.incrementEvents
  val decrementEvent = handlers.decrementEvents

  val box = horizontalList(spacing = 10)(
      listOf(
          spinButton("<", mapOf(onClickKey to decrementEvent)),
          label(TextStyles.mediumBlack, valueText).addAttributes(onClickKey to incrementEvent),
          spinButton(">", mapOf(onClickKey to incrementEvent))
      )
  )
      .copy(
          name = "spinField"
      )

  if (hasFocus)
    box
        .addAttributes(
            onActivateKey to listOf(incrementEvent),
            onClientEventsKey to OnClientEvents(
                listOf(
                    decrementEvent to { it.type == CharacterRigCommands.moveLeft },
                    incrementEvent to { it.type == CharacterRigCommands.moveRight }
                )
            )
        )
  else
    box
}

fun <T> clientEventSpinHandlers(eventType: String, options: List<T>, id: T) =
    SpinHandlers(
        incrementEvents = listOf(ClientEvent(eventType, cycle(options, 1, id))),
        decrementEvents = listOf(ClientEvent(eventType, cycle(options, -1, id)))
    )
