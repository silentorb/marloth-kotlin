package marloth.clienting.gui.menus.views.options

import marloth.clienting.ClientState
import marloth.clienting.gui.StateFlowerTransform
import marloth.clienting.gui.menus.*
import marloth.clienting.gui.menus.general.*
import marloth.clienting.input.developerCommands
import marloth.scenery.enums.DevText
import marloth.scenery.enums.TextId
import silentorb.mythic.bloom.*
import silentorb.mythic.bloom.centered
import simulation.misc.InputEventType
import kotlin.math.max

fun bindingsCommandColumn(rows: List<Box>): Box {
  val breadth = boxList(verticalPlane, defaultMenuGap)(rows).dimensions.x
  return layoutMenuItems(rows) { index, box ->
    fieldWrapper(-1, max(200, breadth))(index, box)
  }
}

fun inputBindingsFlower(clientState: ClientState): StateFlowerTransform = { definitions, state ->
  val textLibrary = definitions.textLibrary
  val profiles = clientState.input.inputProfiles
  val panels = profiles[1L]!!.bindings
      .map { (context, bindings) ->
        val groups = bindings
            .filter { !developerCommands.contains(it.command) }
            .groupBy { it.command }

        val commandRows = groups
            .map { (command, _) ->
              val formattedCommand = command.toString()
                  .replace(Regex("([a-z])([A-Z\\d])")) { m -> m.groups[1]!!.value + " " + m.groups[2]!!.value }
                  .capitalize()

              label(TextStyles.gray, formattedCommand)
            }

        val menuRows = groups
            .map { (_, bindings) ->
              val bindingsText = bindings.joinToString(", ") { binding ->
                val text = definitions.inputEventTypeNames[InputEventType(binding.device, binding.trigger)]
                text ?: "${binding.device} ${binding.trigger}"
              }
              MenuItem(
                  flower = menuTextFlower(bindingsText),
              )
            }

        horizontalList(100)(
            listOf(
                bindingsCommandColumn(commandRows),
                menuFlower(menuRows, state.menuFocusIndex, 200)
            )
        )
      }

  compose(
      dialogSurroundings(definitions),
      flowerMargin(top = 20, bottom = 20)(
          alignSingleFlower(centered, horizontalPlane,
              dialogContentFlower(dialogTitle(textLibrary(TextId.gui_inputBindings)))(
                  scrollableY("bindingsScrolling") { seed ->
                    verticalList(panels)
                  }
              )
          )
      )
  )
}
