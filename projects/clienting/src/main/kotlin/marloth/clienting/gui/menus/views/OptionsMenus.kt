package marloth.clienting.gui.menus.views

import marloth.clienting.input.GuiCommandType
import marloth.clienting.gui.menus.SimpleMenuItem
import marloth.clienting.gui.ViewId
import marloth.clienting.gui.clientEvent
import marloth.clienting.gui.menus.simpleMenuFlower
import marloth.scenery.enums.Text

val inputOptionsMenu =
    simpleMenuFlower(Text.gui_optionsMenu, listOfNotNull(
        SimpleMenuItem(event = clientEvent(GuiCommandType.navigate, ViewId.gamepadOptions), text = Text.gui_gamepadOptions),
        SimpleMenuItem(event = clientEvent(GuiCommandType.navigate, ViewId.mouseOptions), text = Text.gui_mouseOptions)
    ))

val optionsMenu =
    simpleMenuFlower(Text.gui_optionsMenu, listOfNotNull(
        SimpleMenuItem(event = clientEvent(GuiCommandType.navigate, ViewId.audioOptions), text = Text.gui_audioOptions),
        SimpleMenuItem(event = clientEvent(GuiCommandType.navigate, ViewId.displayOptions), text = Text.gui_displayOptions),
        SimpleMenuItem(event = clientEvent(GuiCommandType.navigate, ViewId.inputOptions), text = Text.gui_inputOptions)
    ))
