package marloth.clienting.gui.menus.views

import marloth.clienting.ClientEvent
import marloth.clienting.ClientEventType
import marloth.clienting.gui.ViewId
import marloth.clienting.gui.gameIsActive
import marloth.clienting.input.GuiCommandType
import marloth.clienting.gui.menus.*
import marloth.scenery.enums.Text
import simulation.main.World

fun mainMenuItems(isGameActive: Boolean): List<SimpleMenuItem> = listOfNotNull(
    if (isGameActive) newSimpleMenuItem(event = ClientEvent(GuiCommandType.menuBack), text = Text.menu_continueGame) else null,
    newSimpleMenuItem(event = ClientEvent(GuiCommandType.newGame), text = Text.menu_newGame),
    newSimpleMenuItem(event = ClientEvent(ClientEventType.navigate, ViewId.options), text = Text.gui_optionsMenu),
    newSimpleMenuItem(event = ClientEvent(GuiCommandType.quit), text = Text.menu_quit)
)

fun mainMenu(world: World?) =
    simpleMenuFlower(Text.gui_mainMenu, mainMenuItems(gameIsActive(world)))
