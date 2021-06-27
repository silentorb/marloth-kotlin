package marloth.clienting.gui.menus.views.main

import marloth.clienting.ClientEvent
import marloth.clienting.ClientEventType
import marloth.clienting.gui.ViewId
import marloth.clienting.gui.gameIsActive
import marloth.clienting.input.GuiCommandType
import marloth.clienting.gui.menus.*
import marloth.clienting.gui.menus.general.SimpleMenuItem
import marloth.clienting.gui.menus.general.newSimpleMenuItem
import marloth.clienting.gui.menus.general.simpleMenuFlower
import marloth.scenery.enums.TextId
import simulation.main.World

fun mainMenuItems(isGameActive: Boolean): List<SimpleMenuItem> = listOfNotNull(
    if (isGameActive) newSimpleMenuItem(TextId.menu_continueGame, ClientEvent(GuiCommandType.menuBack)) else null,
    newSimpleMenuItem(TextId.menu_newGame, ClientEvent(GuiCommandType.newGame)),
    newSimpleMenuItem(TextId.gui_optionsMenu, ClientEvent(ClientEventType.drillDown, ViewId.options)),
    newSimpleMenuItem(TextId.gui_manual, ClientEvent(ClientEventType.drillDown, ViewId.manual)),
    newSimpleMenuItem(TextId.menu_quit, ClientEvent(GuiCommandType.quit))
)

fun mainMenu(world: World?) =
    dialogWrapperWithExtras(
        simpleMenuFlower(TextId.gui_mainMenu, mainMenuItems(gameIsActive(world)))
    )
