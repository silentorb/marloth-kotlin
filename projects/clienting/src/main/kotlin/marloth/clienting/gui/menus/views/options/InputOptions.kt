package marloth.clienting.gui.menus.views.options

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
                newSimpleMenuItem(TextId.gui_inputBindings, ClientEvent(ClientEventType.drillDown, ViewId.inputBindings)),
//                newSimpleMenuItem(event = ClientEvent(ClientEventType.drillDown, ViewId.gamepadOptions), text = TextId.gui_gamepadOptions),
//                newSimpleMenuItem(event = ClientEvent(ClientEventType.drillDown, ViewId.mouseOptions), text = TextId.gui_mouseOptions)
            )
        )
    )
