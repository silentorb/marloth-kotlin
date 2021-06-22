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
import silentorb.mythic.spatial.Vector2i
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
          listOf(
              Vector2i(0, row) to { seed: Seed -> label(TextStyles.gray, formattedCommand) },
              Vector2i(1, row) to { seed: Seed -> label(TextStyles.mediumBlack, bindingsText) },
          )
        }
            .flatten()
      }
      .associate { it }
//      .map { (context, bindings) ->


//        val commandRows = groups
//            .map { (command, _) ->
//
//              label(TextStyles.gray, formattedCommand)
//            }
//
//        val menuRows = groups
//            .map { (_, bindings) ->
//
//              MenuItem(
//                  flower = menuTextFlower(bindingsText),
//              )
//            }

//  horizontalList(100)(
//      listOf(
//          bindingsCommandColumn(commandRows),
//          menuFlower(menuRows, state.menuFocusIndex, 200)
//      )
//  )
//      }

  compose(
      dialogSurroundings(definitions),
      flowerMargin(top = 20, bottom = 20)(
          alignSingleFlower(centered, horizontalPlane,
              dialogContentFlower(dialogTitle(textLibrary(TextId.gui_inputBindings)))(
                  scrollableY("bindingsScrolling",
                      tableFlower(cells, Vector2i(100, 40))
                  )
              )
          )
      )
  )
}
