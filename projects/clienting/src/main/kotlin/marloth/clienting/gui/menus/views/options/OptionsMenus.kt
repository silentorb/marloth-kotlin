package marloth.clienting.gui.menus.views.options

import marloth.clienting.ClientEvent
import marloth.clienting.ClientEventType
import marloth.clienting.gui.ViewId
import marloth.clienting.gui.menus.dialogWrapperWithExtras
import marloth.clienting.gui.menus.general.newSimpleMenuItem
import marloth.clienting.gui.menus.general.simpleMenuFlower
import marloth.scenery.enums.TextId

val optionsMenu =
    dialogWrapperWithExtras(
        simpleMenuFlower(TextId.gui_optionsMenu,
            listOfNotNull(
//        newSimpleMenuItem(event = ClientEvent(ClientEventType.navigate, ViewId.audioOptions), text = TextId.gui_audioOptions),
                newSimpleMenuItem(event = ClientEvent(ClientEventType.drillDown, ViewId.displayOptions), text = TextId.gui_displayOptions),
                newSimpleMenuItem(event = ClientEvent(ClientEventType.navigate, ViewId.inputOptions), text = TextId.gui_inputOptions)
            )
        )
    )
