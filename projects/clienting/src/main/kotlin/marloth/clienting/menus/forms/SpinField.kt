package marloth.clienting.menus.forms

import marloth.clienting.input.GuiCommandType
import marloth.clienting.menus.cycle
import marloth.clienting.menus.TextStyles
import marloth.scenery.enums.CharacterRigCommands
import silentorb.mythic.bloom.*

const val previousOptionKey = "silentorb.bloom.previousOption"
const val nextOptionKey = "silentorb.bloom.nextOption"

data class Option(
    val label: String,
    val id: Any
)

fun spinButton(text: String, attributes: Map<String, Any?>): Flower =
    div(attributes = attributes)(
        label(TextStyles.mediumBlack, text)
    )

fun spinField(id: Any, valueText: String): Flower =
    div()(
        horizontalList(spacing = 10)(
            listOf(
                spinButton("<", mapOf(previousOptionKey to id)),
                label(TextStyles.mediumBlack, valueText),
                spinButton(">", mapOf(nextOptionKey to id))
            )
        )
    )

fun <T> cycle(options: List<T>, offset: Int, value: T): T {
  assert(options.any())
  val index = options.indexOf(value)
  assert(index != -1)
  val nextIndex = cycle(index + offset, options.size)
  return options[nextIndex]
}

fun <T> updateSpinField(options: List<T>, commands: List<Any>, hoverBoxes: Boxes, value: T) =
    when {
      commands.contains(CharacterRigCommands.moveRight) ||
          commands.contains(GuiCommandType.menuSelect) -> cycle(options, 1, value)
      commands.contains(CharacterRigCommands.moveLeft) -> cycle(options, -1, value)
      else -> value
    }
