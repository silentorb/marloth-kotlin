package marloth.clienting.menus

import marloth.clienting.PlayerViews
import marloth.clienting.input.GuiCommandType
import silentorb.mythic.ent.Id
import silentorb.mythic.haft.HaftCommands
import simulation.main.Deck

fun nextView(command: Any, view: ViewId): ViewId? =
    when (command) {
      GuiCommandType.menu -> {
        if (view != ViewId.none)
          ViewId.none
        else
          ViewId.mainMenu
      }

      GuiCommandType.characterInfo -> {
        if (view == ViewId.characterInfo)
          ViewId.none
        else if (view == ViewId.none)
          ViewId.characterInfo
        else
          null
      }

      GuiCommandType.menuBack -> {
        if (view != ViewId.none)
          ViewId.none
        else
          null
      }

      GuiCommandType.newGame -> ViewId.none

      else -> view
    }

fun fallBackMenus(deck: Deck, player: Id): ViewId? =
    if (!deck.characters.containsKey(player))
      ViewId.chooseProfessionMenu
    else
      null

fun updateClientCurrentMenus(deck: Deck, commands: HaftCommands, players: List<Id>, playerViews: PlayerViews): PlayerViews {
  return players.associateWith { player ->
    val command = commands.firstOrNull { it.target == player }
    val view = playerViews[player]
    val manuallyChangedView = if (command != null)
      nextView(command.type, view ?: ViewId.none)
    else
      view

    when (manuallyChangedView) {
      null, ViewId.none, ViewId.chooseProfessionMenu -> fallBackMenus(deck, player)
      else -> manuallyChangedView
    }
  }
}
