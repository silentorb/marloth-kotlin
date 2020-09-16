package marloth.clienting.gui.menus

import marloth.clienting.AppOptions
import marloth.clienting.StateFlower
import marloth.clienting.gui.ViewId
import marloth.clienting.gui.emptyViewFlower
import marloth.clienting.gui.menus.views.*
import marloth.clienting.gui.victoryMenu
import marloth.scenery.enums.Text
import silentorb.mythic.ent.Id
import simulation.main.World

fun viewSelect(world: World?, options: AppOptions, view: ViewId?, player: Id): StateFlower? {
  return when (view) {
    ViewId.audioOptions -> emptyViewFlower
    ViewId.displayChangeConfirmation -> displayChangeConfirmationFlower
    ViewId.displayOptions -> displayOptionsFlower()
    ViewId.gamepadOptions -> emptyViewFlower
    ViewId.inputOptions -> inputOptionsMenu
    ViewId.mouseOptions -> emptyViewFlower
    ViewId.options -> optionsMenu
    ViewId.characterInfo -> characterInfoViewOrChooseAbilityMenu(world!!.deck, player)
    ViewId.chooseProfessionMenu -> simpleMenuFlower(Text.gui_chooseProfessionMenu, chooseProfessionMenu(player))
    ViewId.mainMenu -> mainMenu(world)
    ViewId.merchant -> merchantView(world!!.deck, player)
    ViewId.victory -> simpleMenuFlower(Text.gui_victory, victoryMenu())
    null -> null
  }
}
