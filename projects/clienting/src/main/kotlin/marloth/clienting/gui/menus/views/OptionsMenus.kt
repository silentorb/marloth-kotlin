package marloth.clienting.gui.menus.views

import marloth.clienting.ClientEvent
import marloth.clienting.ClientEventType
import marloth.clienting.gui.ViewId
import marloth.clienting.gui.menus.dialogWrapperWithExtras
import marloth.clienting.gui.menus.general.newSimpleMenuItem
import marloth.clienting.gui.menus.general.simpleMenuFlower
import marloth.scenery.enums.TextId

val inputOptionsMenu =
    dialogWrapperWithExtras(
        simpleMenuFlower(TextId.gui_optionsMenu,
            listOfNotNull(
                newSimpleMenuItem(event = ClientEvent(ClientEventType.navigate, ViewId.gamepadOptions), text = TextId.gui_gamepadOptions),
                newSimpleMenuItem(event = ClientEvent(ClientEventType.navigate, ViewId.mouseOptions), text = TextId.gui_mouseOptions)
            )
        )
    )

val optionsMenu =
    dialogWrapperWithExtras(
        simpleMenuFlower(TextId.gui_optionsMenu,
            listOfNotNull(
//        newSimpleMenuItem(event = ClientEvent(ClientEventType.navigate, ViewId.audioOptions), text = TextId.gui_audioOptions),
                newSimpleMenuItem(event = ClientEvent(ClientEventType.navigate, ViewId.displayOptions), text = TextId.gui_displayOptions),
//        newSimpleMenuItem(event = ClientEvent(ClientEventType.navigate, ViewId.inputOptions), text = TextId.gui_inputOptions)
            )
        )
    )
