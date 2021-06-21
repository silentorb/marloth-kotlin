package marloth.clienting.gui.menus.views.options

import marloth.clienting.Client
import marloth.clienting.ClientState
import marloth.clienting.gui.StateFlowerTransform
import marloth.clienting.gui.menus.*
import marloth.clienting.gui.menus.general.*
import marloth.clienting.gui.menus.general.menuFlower
import marloth.clienting.input.developerCommands
import marloth.scenery.enums.DevText
import marloth.scenery.enums.TextId
import silentorb.mythic.bloom.*
import silentorb.mythic.bloom.centered
import simulation.misc.InputEventType

fun inputBindingsFlower(clientState: ClientState): StateFlowerTransform = { definitions, state ->
  val textLibrary = definitions.textLibrary
  val profiles = clientState.input.inputProfiles
  val panels = profiles[1L]!!.bindings
      .map { (context, bindings) ->
        val groups = bindings
            .filter { !developerCommands.contains(it.command)}
            .groupBy { it.command }
        val rows = groups.map { (command, bindings) ->
          val bindingsText = bindings
              .map { binding ->
                val text = definitions.inputEventTypeNames[InputEventType(binding.device, binding.trigger)]
                text ?: "${binding.device} ${binding.trigger}"
              }
              .joinToString(", ")

          val formmattedCommand = command.toString()
              .replace(Regex("([a-z])([A-Z\\d])")) { m -> m.groups[1]!!.value + " " + m.groups[2]!!.value }
              .capitalize()

          SimpleMenuItem(
              text = DevText("$formmattedCommand   $bindingsText")
          )
        }
        simpleMenuFlower(DevText(context.name.capitalize()), rows)(definitions, state)
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
