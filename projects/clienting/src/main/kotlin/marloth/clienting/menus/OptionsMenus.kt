package marloth.clienting.menus

import marloth.clienting.input.GuiCommandType
import marloth.scenery.enums.Text
import simulation.misc.Definitions

fun inputOptionsMenu(definitions: Definitions) =
    menuFlower(definitions, Text.gui_optionsMenu, listOfNotNull(
        SimpleMenuItem(event = clientEvent(GuiCommandType.navigate, ViewId.gamepadOptions), text = Text.gui_gamepadOptions),
        SimpleMenuItem(event = clientEvent(GuiCommandType.navigate, ViewId.mouseOptions), text = Text.gui_mouseOptions)
    ))

fun optionsMenu(definitions: Definitions) =
    menuFlower(definitions, Text.gui_optionsMenu, listOfNotNull(
        SimpleMenuItem(event = clientEvent(GuiCommandType.navigate, ViewId.audioOptions), text = Text.gui_audioOptions),
        SimpleMenuItem(event = clientEvent(GuiCommandType.navigate, ViewId.displayOptions), text = Text.gui_displayOptions),
        SimpleMenuItem(event = clientEvent(GuiCommandType.navigate, ViewId.inputOptions), text = Text.gui_inputOptions)
    ))
