package marloth.clienting.gui.menus.views.options

import marloth.clienting.gui.EventUnion
import marloth.clienting.gui.StateFlowerTransform
import marloth.clienting.gui.menus.TextStyles
import marloth.clienting.gui.menus.dialogContentFlower
import marloth.clienting.gui.menus.dialogSurroundings
import marloth.clienting.gui.menus.dialogTitle
import marloth.clienting.gui.menus.general.addMenuItemInteractivity
import marloth.clienting.gui.menus.general.stretchyFieldWrapper
import marloth.clienting.gui.menus.logic.menuLengthKey
import marloth.clienting.input.*
import marloth.scenery.enums.TextId
import silentorb.mythic.bloom.*
import silentorb.mythic.haft.Binding
import silentorb.mythic.happenings.Command
import silentorb.mythic.spatial.Vector2i
import simulation.misc.InputEventType

const val bindingsEditingKey = "silentorb.bindingsEditing"
const val isolatedInput = "silentorb.isolatedInput"
const val commandKey = "silentorb.command"
const val newInputOptionsCommand = "newInputOptions"

fun updateInputOptions(command: Command, options: InputOptions): InputOptions =
    when (command.type) {
      newInputOptionsCommand -> {
        val newOptions = command.value as? InputOptions
        newOptions ?: options
      }
      else -> options
    }

val bindingCommandKeys: Map<InputContext, List<Any>> = defaultInputBindings()
    .mapValues { (_, bindings) ->
      bindings
          .filter { !developerCommands.contains(it.command) }
          .map { it.command }
          .distinct()
    }

fun bindingInputItem(row: Int, text: String, events: List<EventUnion>) =
    addMenuItemInteractivity(row, events,
        stretchyFieldWrapper(row,
            label(TextStyles.mediumBlack, text)
        )
    )

fun inputBindingsFlower(options: InputOptions): StateFlowerTransform = { definitions, state ->
  val textLibrary = definitions.textLibrary
  val profiles = options.profiles
  var menuLength = 0
  val firstRow = listOf(
      Vector2i(1, 0) to withLogic(onActivate { _, _ ->
        val newInputOptions = options.copy(
            profiles = defaultInputProfiles(),
        )
        mapOf(
            commandKey to Command(newInputOptionsCommand, value = newInputOptions)
        )
      })(bindingInputItem(0, "Reset to Defaults", listOf())),
  )
  val bindingsEditing = state.bloomState[bindingsEditingKey] as? Int
  val profileBindings = profiles[1L]!!.bindings
  val cells = profileBindings
      .entries
      .fold(firstRow) { a, (context, contextBindings) ->
        val rowOffset = a.maxByOrNull { it.first.y }?.first?.y?.plus(1) ?: 0
        val groups = bindingCommandKeys[context]!!
            .associateWith { command -> contextBindings.filter { it.command == command } }

        a + listOf(
            Vector2i(0, rowOffset) to centered(label(TextStyles.mediumBlack, context.name.capitalize())),
        ) + groups.entries.mapIndexed { index, (command, bindings) ->
          val row = rowOffset + 2 + index
          val formattedCommand = command.toString()
              .replace(Regex("([a-z])([A-Z\\d])")) { m -> m.groups[1]!!.value + " " + m.groups[2]!!.value }
              .capitalize()

          val isEditingRow = row == bindingsEditing
          val bindingsText = if (isEditingRow)
            "Press Any Key"
          else
            bindings.joinToString(", ") { binding ->
              val text = definitions.inputEventTypeNames[InputEventType(binding.device, binding.trigger)]
              text ?: "${binding.device} ${binding.trigger}"
            }

          val activateLogic = if (isEditingRow)
            withLogic { input, _ ->
              val event = input.deviceStates.last().events.firstOrNull()
              if (event != null) {
                val newBinding = Binding(
                    device = event.device,
                    trigger = event.index,
                    command = command,
                )
                val newProfile = profiles[1L]!!.copy(
                    bindings = profileBindings.plus(
                        context to contextBindings.plus(
                            newBinding
                        )
                    )
                )

                val newInputOptions = options.copy(
                    profiles = options.profiles + mapOf(
                        1L to newProfile,
                    )
                )

                mapOf(
                    bindingsEditingKey to deleteMe,
                    isolatedInput to deleteMe,
                    commandKey to Command(newInputOptionsCommand, value = newInputOptions),
                )
              } else
                mapOf()
            }
          else
            withLogic(
                onActivate { _, _ ->
                  mapOf(
                      bindingsEditingKey to row,
                      isolatedInput to row,
                  )
                }
            )

          val menuItem = activateLogic(
              bindingInputItem(row, bindingsText, listOf())
          )
          ++menuLength

          listOf(
              Vector2i(0, row) to { seed: Seed -> label(TextStyles.gray, formattedCommand) },
              Vector2i(1, row) to menuItem,
          )
        }
            .flatten()
      }
      .associate { it }

  compose(
      dialogSurroundings(definitions),
      flowerMargin(top = 20, bottom = 20)(
          alignSingleFlower(centered, horizontalPlane,
              dialogContentFlower(dialogTitle(textLibrary(TextId.gui_inputBindings)))(
                  scrollableY("bindingsScrolling",
                      withAttributes(menuLengthKey to menuLength)(tableFlower(cells, Vector2i(100, 40)))
                  )
              )
          )
      )
  )
}
