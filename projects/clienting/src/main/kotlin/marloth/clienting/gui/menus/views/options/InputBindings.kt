package marloth.clienting.gui.menus.views.options

import marloth.clienting.ClientState
import marloth.clienting.gui.StateFlowerTransform
import marloth.clienting.gui.menus.*
import marloth.clienting.gui.menus.general.*
import marloth.clienting.gui.menus.logic.menuLengthKey
import marloth.clienting.input.developerCommands
import marloth.scenery.enums.TextId
import silentorb.mythic.bloom.*
import silentorb.mythic.bloom.centered
import silentorb.mythic.spatial.Vector2i
import simulation.misc.InputEventType

fun inputBindingsFlower(clientState: ClientState): StateFlowerTransform = { definitions, state ->
  val textLibrary = definitions.textLibrary
  val profiles = clientState.input.inputProfiles
  var menuLength = 0
  val cells = profiles[1L]!!.bindings
      .entries
      .fold(listOf<Pair<Vector2i, Flower>>()) { a, (context, bindings) ->
        val rowOffset = a.maxByOrNull { it.first.y }?.first?.y?.plus(1) ?: 0
        val groups = bindings
            .filter { !developerCommands.contains(it.command) }
            .groupBy { it.command }

        a + groups.entries.mapIndexed { index, (command, bindings) ->
          val formattedCommand = command.toString()
              .replace(Regex("([a-z])([A-Z\\d])")) { m -> m.groups[1]!!.value + " " + m.groups[2]!!.value }
              .capitalize()

          val bindingsText = bindings.joinToString(", ") { binding ->
            val text = definitions.inputEventTypeNames[InputEventType(binding.device, binding.trigger)]
            text ?: "${binding.device} ${binding.trigger}"
          }

          val row = rowOffset + index
//          val key = "binding_$command"

          val menuItem = addMenuItemInteractivity(row, listOf(),
              stretchyFieldWrapper(row,
                  label(TextStyles.mediumBlack, bindingsText)
              )
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
