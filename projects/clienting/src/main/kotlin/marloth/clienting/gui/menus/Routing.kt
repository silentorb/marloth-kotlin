package marloth.clienting.gui.menus

import marloth.clienting.AppOptions
import marloth.clienting.ClientState
import marloth.clienting.StateFlowerTransform
import marloth.clienting.gui.ViewId
import marloth.clienting.gui.emptyViewFlower
import marloth.clienting.gui.menus.general.simpleMenuFlower
import marloth.clienting.gui.menus.views.*
import marloth.clienting.gui.victoryMenu
import marloth.scenery.enums.TextId
import silentorb.mythic.ent.Id
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
    ViewId.characterInventory -> characterInventoryView(world!!.deck, player)
    ViewId.characterStatus -> characterInfoView(world!!.deck, player)
    ViewId.characterContracts -> characterContractsView(world!!.deck, player)
    ViewId.chooseProfessionMenu -> dialogWrapper(simpleMenuFlower(TextId.gui_chooseProfessionMenu, chooseProfessionMenu(player)))
    ViewId.mainMenu -> mainMenu(world)
    ViewId.conversation -> conversationView(world!!.deck, player)
    ViewId.victory -> dialogWrapper(simpleMenuFlower(TextId.gui_victory, victoryMenu()))
    null -> null
  }
}
