package marloth.clienting.gui.menus.views.options

import marloth.clienting.ClientEvent
import marloth.clienting.ClientEventType
import marloth.clienting.gui.StateFlower
import marloth.clienting.gui.menus.*
import marloth.clienting.gui.menus.general.MenuItem
import marloth.clienting.gui.menus.general.menuFlower
import marloth.clienting.gui.menus.general.menuTextFlower
import marloth.scenery.enums.TextId
import silentorb.mythic.bloom.*

val displayChangeConfirmationFlower: StateFlower =
    dialogWrapperWithExtras { definitions, state ->
      val title = dialogTitle(definitions.textLibrary(TextId.gui_query_saveDisplayChanges))
      val menuBox = menuFlower(listOf(
          MenuItem(
              flower = menuTextFlower(definitions.textLibrary(TextId.gui_yes)),
              events = listOf(
                  ClientEvent(ClientEventType.saveDisplayChange),
                  ClientEvent(ClientEventType.menuBack)
              )
          ),
          MenuItem(
              flower = menuTextFlower(definitions.textLibrary(TextId.gui_no)),
              events = listOf(
                  ClientEvent(ClientEventType.revertDisplayChanges),
                  ClientEvent(ClientEventType.menuBack)
              )
          ),
      ), state.menuFocusIndex, title.dimensions.x)

      dialogContentWithTitle(titleBar(title))(
          boxList(verticalPlane)(
              listOf(
                  menuBox,
                  alignSingle(centered, horizontalPlane,
                      label(TextStyles.mediumBlack, ((state.displayChange?.timeout ?: 0.0).toInt() + 1).toString())
                  )(menuBox.dimensions.x)
              )
          )
      )
    }
