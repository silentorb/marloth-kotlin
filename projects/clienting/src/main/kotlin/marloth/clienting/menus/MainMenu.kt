package marloth.clienting.menus

import marloth.clienting.hud.versionDisplay
import marloth.clienting.input.GuiCommandType
import marloth.clienting.resources.UiTextures
import marloth.scenery.enums.Text
import silentorb.mythic.bloom.next.compose
import silentorb.mythic.bloom.next.div
import simulation.main.World
import simulation.misc.Definitions

fun mainMenuItems(isGameActive: Boolean): List<SimpleMenuItem> = listOfNotNull(
    if (isGameActive) SimpleMenuItem(command = GuiCommandType.menuBack, text = Text.menu_continueGame) else null,
    SimpleMenuItem(command = GuiCommandType.newGame, text = Text.menu_newGame),
    SimpleMenuItem(command = GuiCommandType.quit, text = Text.menu_quit)
)

fun mainMenu(textResources: TextResources, definitions: Definitions, world: World?) =
    compose(
        menuFlower(textResources, Text.gui_mainMenu, mainMenuItems(gameIsActive(world))),
        versionDisplay(definitions.applicationInfo.version)
    )
