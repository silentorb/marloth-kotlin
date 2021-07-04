package marloth.clienting.gui.menus

import marloth.clienting.AppOptions
import marloth.clienting.ClientState
import marloth.clienting.gui.StateFlower
import marloth.clienting.gui.ViewId
import marloth.clienting.gui.emptyViewFlower
import marloth.clienting.gui.menus.general.simpleMenuFlower
import marloth.clienting.gui.menus.views.character.characterContractsView
import marloth.clienting.gui.menus.views.character.characterInfoView
import marloth.clienting.gui.menus.views.character.characterInventoryView
import marloth.clienting.gui.menus.views.character.chooseProfessionMenu
import marloth.clienting.gui.menus.views.interaction.*
import marloth.clienting.gui.menus.views.main.*
import marloth.clienting.gui.menus.views.options.*
import marloth.clienting.gui.victoryMenu
import marloth.scenery.enums.TextId
import silentorb.mythic.ent.Id
import simulation.main.World

fun viewSelect(world: World?, options: AppOptions, clientState: ClientState, view: ViewId?, player: Id): StateFlower? {
  return when (view) {
    ViewId.audioOptions -> emptyViewFlower
    ViewId.dev -> devView()
    ViewId.displayChangeConfirmation -> displayChangeConfirmationFlower
    ViewId.displayOptions -> displayOptionsFlower(clientState)
    ViewId.gamepadOptions -> emptyViewFlower
    ViewId.inputBindings -> inputBindingsFlower(options.input)
    ViewId.inputOptions -> inputOptionsMenu
    ViewId.mouseOptions -> emptyViewFlower
    ViewId.options -> optionsMenu
    ViewId.characterInventory -> characterInventoryView(world!!.deck, player)
    ViewId.characterStatus -> characterInfoView(world!!.deck, player)
    ViewId.characterContracts -> characterContractsView(world!!.deck, player)
    ViewId.chooseProfessionMenu -> dialogWrapper(simpleMenuFlower(TextId.gui_chooseProfessionMenu, chooseProfessionMenu(player)))
    ViewId.mainMenu -> mainMenu(world)
    ViewId.manual -> manualView(options.input)
    ViewId.conversation -> conversationView(world!!.deck, player)
    ViewId.conversationActiveContracts -> clientActiveContractsView(world!!.deck, player)
    ViewId.conversationAvailableContracts -> clientAvailableContractsView(world!!.deck, player)
    ViewId.conversationMerchandise -> merchantView(world!!.deck, player)
    ViewId.victory -> dialogWrapper(simpleMenuFlower(TextId.gui_victory, victoryMenu()))
    null -> null
  }
}
