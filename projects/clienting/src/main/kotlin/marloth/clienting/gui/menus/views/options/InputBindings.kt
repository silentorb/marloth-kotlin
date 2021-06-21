package marloth.clienting.gui.menus.views.options

import marloth.clienting.ClientState
import marloth.clienting.gui.StateFlowerTransform
import marloth.clienting.gui.menus.*
import marloth.clienting.gui.menus.general.*
import marloth.clienting.gui.menus.general.menuFlower
import marloth.scenery.enums.DevText
import marloth.scenery.enums.TextId
import silentorb.mythic.bloom.*
import silentorb.mythic.bloom.centered

fun inputBindingsFlower(clientState: ClientState): StateFlowerTransform = { definitions, state ->
  val textLibrary = definitions.textLibrary
  val profiles = clientState.input.inputProfiles
  val panels = profiles[1L]!!.bindings
      .map { (context, bindings) ->
        val rows = bindings.map { binding ->
          SimpleMenuItem(
              text = DevText(binding.trigger.toString())
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
