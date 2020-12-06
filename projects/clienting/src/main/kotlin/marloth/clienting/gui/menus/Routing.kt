package marloth.clienting.gui.menus

import marloth.clienting.AppOptions
import marloth.clienting.ClientState
import marloth.clienting.StateFlower
import marloth.clienting.StateFlowerTransform
import marloth.clienting.gui.ViewId
import marloth.clienting.gui.emptyViewFlower
import marloth.clienting.gui.menus.views.*
import marloth.clienting.gui.victoryMenu
import marloth.scenery.enums.ClientCommand
import marloth.scenery.enums.Text
import silentorb.mythic.ent.Id
import simulation.main.Deck
import simulation.main.World

fun viewSelect(world: World?, options: AppOptions, clientState: ClientState, view: ViewId?, player: Id): StateFlowerTransform? {
  return when (view) {
    ViewId.audioOptions -> emptyViewFlower
    ViewId.displayChangeConfirmation -> displayChangeConfirmationFlower
    ViewId.displayOptions -> displayOptionsFlower(clientState)
    ViewId.gamepadOptions -> emptyViewFlower
    ViewId.inputOptions -> inputOptionsMenu
    ViewId.mouseOptions -> emptyViewFlower
    ViewId.options -> optionsMenu
    ViewId.characterInfo -> characterInfoViewOrChooseAbilityMenu(world!!.deck, player)
    ViewId.chooseProfessionMenu -> dialogWrapper(simpleMenuFlower(Text.gui_chooseProfessionMenu, chooseProfessionMenu(player)))
    ViewId.mainMenu -> mainMenu(world)
    ViewId.merchant -> merchantView(world!!.deck, player)
    ViewId.victory -> dialogWrapper(simpleMenuFlower(Text.gui_victory, victoryMenu()))
    null -> null
  }
}
