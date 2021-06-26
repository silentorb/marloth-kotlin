package marloth.clienting.gui.menus.views.options

import marloth.clienting.ClientEventType
import marloth.clienting.gui.ViewId
import marloth.clienting.gui.menus.dialogWrapperWithExtras
import marloth.clienting.gui.menus.general.newSimpleMenuItem
import marloth.clienting.gui.menus.general.simpleMenuFlower
import marloth.scenery.enums.TextId
import silentorb.mythic.happenings.Command

val optionsMenu =
    dialogWrapperWithExtras(
        simpleMenuFlower(TextId.gui_optionsMenu,
            listOfNotNull(
//        newSimpleMenuItem(event = ClientEvent(ClientEventType.navigate, ViewId.audioOptions), text = TextId.gui_audioOptions),
                newSimpleMenuItem(TextId.gui_displayOptions, Command(ClientEventType.drillDown, ViewId.displayOptions)),
                newSimpleMenuItem(TextId.gui_inputOptions, Command(ClientEventType.drillDown, ViewId.inputOptions))
            )
        )
    )
