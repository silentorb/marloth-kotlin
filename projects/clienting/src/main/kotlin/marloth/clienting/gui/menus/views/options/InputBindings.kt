package marloth.clienting.gui.menus.views.options

import marloth.clienting.gui.EventUnion
import marloth.clienting.gui.StateFlower
import marloth.clienting.gui.menus.TextStyles
import marloth.clienting.gui.menus.dialogContentFlower
import marloth.clienting.gui.menus.dialogSurroundings
import marloth.clienting.gui.menus.dialogTitle
import marloth.clienting.gui.menus.general.addMenuItemInteractivity
import marloth.clienting.gui.menus.general.stretchyFieldWrapper
import marloth.clienting.gui.menus.logic.menuLengthKey
import marloth.clienting.input.*
import marloth.scenery.enums.TextId
import org.lwjgl.glfw.GLFW
import silentorb.mythic.bloom.*
import silentorb.mythic.haft.Binding
import silentorb.mythic.haft.Bindings
import silentorb.mythic.haft.DeviceIndexes
import silentorb.mythic.haft.GAMEPAD_BUTTON_X
import silentorb.mythic.happenings.Command
import silentorb.mythic.spatial.Vector2i
import simulation.misc.Definitions
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
            label(TextStyles.smallBlack, text)
        )
    )

fun newProfileCommand(options: InputOptions, newProfile: InputProfile): Map<String, Command> {
  val newInputOptions = options.copy(
      profiles = options.profiles + mapOf(
          1L to newProfile,
      )
  )
  return mapOf(
      commandKey to Command(newInputOptionsCommand, value = newInputOptions),
  )
}

fun formatBindingsText(definitions: Definitions, bindings: Bindings) =
    bindings.joinToString(", ") { binding ->
      val text = definitions.inputEventTypeNames[InputEventType(binding.device, binding.index)]
      text ?: "${binding.device} ${binding.index}"
    }


fun inputBindingsFlower(options: InputOptions): StateFlower = { definitions, state ->
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
      })(bindingInputItem(1, "Reset to Defaults", listOf())),
  )
  val bindingsEditing = state.bloomState[bindingsEditingKey] as? Int
  val profile = profiles[defaultInputProfile]!!
  val profileBindings = profile.bindings
  val focusIndex = getFocusIndex(state.bloomState)

  val cells = profileBindings
      .entries
      .fold(firstRow) { a, (context, contextBindings) ->
        val rowOffset = a.maxByOrNull { it.first.y }?.first?.y?.plus(1) ?: 0
        val groups = bindingCommandKeys[context]!!
            .associateWith { command -> contextBindings.filter { it.command == command } }

        a + listOf(
            Vector2i(0, rowOffset) to centered(label(TextStyles.h3, context.name.capitalize())),
        ) + groups.entries.mapIndexed { index, (command, bindings) ->
          val menuIndex = rowOffset + index + 2
          val row = menuIndex - 1
          val formattedCommand = command.toString()
              .replace(Regex("([a-z])([A-Z\\d])")) { m -> m.groups[1]!!.value + " " + m.groups[2]!!.value }
              .capitalize()

          val isEditingRow = menuIndex == bindingsEditing
          val bindingsText = if (isEditingRow)
            "Press Any Key"
          else
            formatBindingsText(definitions, bindings)

          val logic = if (isEditingRow)
            withLogic { input, _ ->
              val event = input.deviceStates.last().events.firstOrNull()
              if (event != null) {
                val previousBinding = contextBindings
                    .firstOrNull { it.device == event.device && it.index == event.index }

                val newBinding = Binding(
                    device = event.device,
                    index = event.index,
                    command = command,
                )
                val newProfile = profile.copy(
                    bindings = profileBindings.plus(
                        context to contextBindings
                            .minus(listOfNotNull(previousBinding))
                            .plus(newBinding)
                    )
                )

                mapOf(
                    bindingsEditingKey to deleteMe,
                    isolatedInput to deleteMe,
                ) + newProfileCommand(options, newProfile)
              } else
                mapOf()
            }
          else
            withLogic(
                composeLogicNotNull(
                    onActivate { _, _ ->
                      mapOf(
                          bindingsEditingKey to menuIndex,
                          isolatedInput to menuIndex,
                      )
                    },
                    if (menuIndex == focusIndex)
                      { input, _ ->
                        if (input.isPressed(DeviceIndexes.keyboard, GLFW.GLFW_KEY_DELETE) ||
                            input.isPressed(DeviceIndexes.gamepad, GAMEPAD_BUTTON_X)) {
                          val newProfile = profile.copy(
                              bindings = profileBindings.plus(
                                  context to contextBindings
                                      .filter { it.command != command }
                              )
                          )

                          newProfileCommand(options, newProfile)
                        } else
                          mapOf()
                      }
                    else
                      null
                )
            )

          val menuItem = logic(
              bindingInputItem(menuIndex, bindingsText, listOf())
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
                      withAttributes(menuLengthKey to menuLength)(tableFlower(cells, Vector2i(40, 40)))
                  )
              )
          )
      )
  )
}
