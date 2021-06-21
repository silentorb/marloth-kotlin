package marloth.clienting.gui.menus.views.interaction

import marloth.clienting.gui.menus.general.MenuItem
import marloth.clienting.gui.menus.general.MenuItemFlower
import marloth.clienting.gui.menus.general.menuFlower
import marloth.clienting.gui.menus.general.menuTextFlower
import silentorb.mythic.bloom.boxList2
import silentorb.mythic.bloom.horizontalPlane
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Command
import simulation.entities.Contract
import simulation.entities.ContractCommands
import simulation.entities.ContractDefinition
import simulation.entities.ContractStatus
import simulation.main.NewHand
import simulation.misc.Definitions

fun contractItemDisplay(definitions: Definitions, definition: ContractDefinition): MenuItemFlower {
  val name = definitions.textLibrary(definition.name)
  val reward = definition.reward
  return menuTextFlower("$name $$reward", true)
}

fun availableContractMenuItem(definitions: Definitions, client: Id, player: Id, contract: Id, definition: ContractDefinition): MenuItem {
  val events = listOf(
      NewHand(
          components = listOf(
              Contract(
                  client = client,
                  agent = player,
                  definition = definition,
                  status = ContractStatus.active,
              )
          )
      ),
      Command(ContractCommands.removeAvailableContract, target = client, value = contract)
  )

  return MenuItem(
      flower = contractItemDisplay(definitions, definition),
      events = events
  )
}

val clientAvailableContractsView = conversationPage { deck, player, other, character, otherCharacter ->
  { definitions, state ->
    val menu = otherCharacter.availableContracts
        .map { (id, contract) ->
          availableContractMenuItem(definitions, other, player, id, contract)
        }

    boxList2(
        horizontalPlane, 10,
        menuFlower(menu, state.menuFocusIndex, 200),
    )
  }
}
