package marloth.clienting.menus.views

import marloth.clienting.input.GuiCommandType
import marloth.clienting.menus.SimpleMenuItem
import marloth.clienting.menus.ViewId
import marloth.clienting.menus.clientEvent
import marloth.clienting.menus.simpleMenuFlower
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
