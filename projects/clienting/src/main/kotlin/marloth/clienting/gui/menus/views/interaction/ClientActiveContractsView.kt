package marloth.clienting.gui.menus.views.interaction

import marloth.clienting.gui.menus.general.MenuItem
import marloth.clienting.gui.menus.general.menuFlower
import silentorb.mythic.bloom.boxList2
import silentorb.mythic.bloom.horizontalPlane
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Command
import simulation.entities.ContractCommands
import simulation.entities.ContractDefinition
import simulation.entities.ContractStatus
import simulation.entities.getContracts
import simulation.happenings.requestCommand
import simulation.misc.Definitions

fun activeContractMenuItem(definitions: Definitions, client: Id, player: Id, contract: Id, definition: ContractDefinition): MenuItem {
  val events = listOf(
      requestCommand(ContractCommands.contractCompleted, player, client, contract),
  )

  return MenuItem(
      flower = contractItemDisplay(definitions, definition),
      events = events
  )
}

val clientActiveContractsView = conversationPage { deck, player, other, character, otherCharacter ->
  { definitions, state ->
    val menu = getContracts(deck, client = other, agent = player, status = ContractStatus.active)
        .map { (id, contract) ->
          activeContractMenuItem(definitions, other, player, id, contract.definition)
        }

    boxList2(
        horizontalPlane, 10,
        menuFlower(menu, state.menuFocusIndex, 100),
    )
  }
}
