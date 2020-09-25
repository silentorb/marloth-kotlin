package marloth.clienting.gui.menus.views

import marloth.clienting.ClientEvent
import marloth.clienting.ClientEventType
import marloth.clienting.gui.ViewId
import marloth.clienting.gui.menus.newSimpleMenuItem
import marloth.clienting.gui.menus.simpleMenuFlower
import marloth.scenery.enums.Text

val inputOptionsMenu =
    simpleMenuFlower(Text.gui_optionsMenu, listOfNotNull(
        newSimpleMenuItem(event = ClientEvent(ClientEventType.navigate, ViewId.gamepadOptions), text = Text.gui_gamepadOptions),
        newSimpleMenuItem(event = ClientEvent(ClientEventType.navigate, ViewId.mouseOptions), text = Text.gui_mouseOptions)
    ))

val optionsMenu =
    simpleMenuFlower(Text.gui_optionsMenu, listOfNotNull(
        newSimpleMenuItem(event = ClientEvent(ClientEventType.navigate, ViewId.audioOptions), text = Text.gui_audioOptions),
        newSimpleMenuItem(event = ClientEvent(ClientEventType.navigate, ViewId.displayOptions), text = Text.gui_displayOptions),
        newSimpleMenuItem(event = ClientEvent(ClientEventType.navigate, ViewId.inputOptions), text = Text.gui_inputOptions)
    ))
