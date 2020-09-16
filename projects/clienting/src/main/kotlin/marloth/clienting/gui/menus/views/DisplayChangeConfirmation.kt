package marloth.clienting.gui.menus.views

import marloth.clienting.ClientEvent
import marloth.clienting.ClientEventType
import marloth.clienting.StateFlower
import marloth.clienting.gui.menus.*
import marloth.clienting.input.GuiCommandType
import marloth.scenery.enums.Text
import silentorb.mythic.bloom.*

val displayChangeConfirmationFlower: StateFlower = { definitions, state ->
  val title = dialogTitle(definitions.textLibrary(Text.gui_query_saveDisplayChanges))
  val menuBox = menuFlower(listOf(
      MenuItem(
          flower = menuTextFlower(definitions.textLibrary(Text.gui_yes)),
          events = listOf(
              ClientEvent(ClientEventType.saveDisplayChange),
              ClientEvent(ClientEventType.menuBack)
          )
      ),
      MenuItem(
          flower = menuTextFlower(definitions.textLibrary(Text.gui_no)),
          events = listOf(
              ClientEvent(ClientEventType.revertDisplayChanges),
              ClientEvent(ClientEventType.menuBack)
          )
      ),
  ), state.menuFocusIndex, title.dimensions.x)

  dialogContent(title)(
      boxList(verticalPlane)(
          listOf(
              menuBox,
              centeredAxis(horizontalPlane)(
                  label(TextStyles.mediumBlack, ((state.displayChange?.timeout ?: 0.0).toInt() + 1).toString())
              )(menuBox.dimensions.x)
          )
      )
  )
}
