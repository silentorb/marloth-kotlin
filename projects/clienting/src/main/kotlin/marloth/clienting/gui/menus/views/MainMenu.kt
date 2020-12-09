package marloth.clienting.gui.menus.views

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
    if (isGameActive) newSimpleMenuItem(event = ClientEvent(GuiCommandType.menuBack), text = TextId.menu_continueGame) else null,
    newSimpleMenuItem(event = ClientEvent(GuiCommandType.newGame), text = TextId.menu_newGame),
    newSimpleMenuItem(event = ClientEvent(ClientEventType.navigate, ViewId.options), text = TextId.gui_optionsMenu),
    newSimpleMenuItem(event = ClientEvent(GuiCommandType.quit), text = TextId.menu_quit)
)

fun mainMenu(world: World?) =
    dialogWrapperWithExtras(
        simpleMenuFlower(TextId.gui_mainMenu, mainMenuItems(gameIsActive(world)))
    )
