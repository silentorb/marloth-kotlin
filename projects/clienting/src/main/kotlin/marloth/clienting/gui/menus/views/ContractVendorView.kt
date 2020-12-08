package marloth.clienting.gui.menus.views

import marloth.clienting.StateFlowerTransform
import marloth.clienting.gui.menus.*
import silentorb.mythic.bloom.boxList2
import silentorb.mythic.bloom.horizontalPlane
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Command
import simulation.characters.Character
import simulation.entities.Contract
import simulation.entities.ContractDefinition
import simulation.entities.ContractStatus
import simulation.entities.removeAvailableContractCommand
import simulation.main.Deck
import simulation.main.NewHand
import simulation.misc.Definitions

fun contractMenuItem(definitions: Definitions, client: Id, player: Id, availableContract: Id, definition: ContractDefinition): MenuItem {
  val name = definitions.textLibrary(definition.name)
  val reward = definition.reward
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
      Command(removeAvailableContractCommand, target = client, value = availableContract)
  )

  return MenuItem(
      flower = menuTextFlower("$name $$reward", true),
      events = events
  )
}

fun contractVendorView(
    deck: Deck,
    player: Id,
    merchant: Id,
    customerCharacter: Character,
    merchantCharacter: Character
): StateFlowerTransform = dialogWrapper { definitions, state ->
  val menu = merchantCharacter.availableContracts
      .map { (id, contract) ->
        contractMenuItem(definitions, merchant, player, id, contract)
      }

  dialog(definitions, merchantCharacter.definition.name,
      boxList2(
          horizontalPlane, 10,
          menuFlower(menu, state.menuFocusIndex, 100),
      )
  )
}
