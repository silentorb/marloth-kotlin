package simulation.characters

import marloth.scenery.enums.CharacterCommands
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.pipe2
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Commands
import silentorb.mythic.happenings.Events
import silentorb.mythic.physics.BulletState
import silentorb.mythic.randomly.Dice
import simulation.accessorize.ChooseImprovedAccessory
import simulation.accessorize.newAccessoryChoice
import simulation.accessorize.updateUtilityItem
import simulation.combat.general.ResourceTypes
import simulation.combat.general.modifyResourceWithEvents
import simulation.entities.ContractCommands
import simulation.entities.updateAvailableContracts
import simulation.main.Deck
import simulation.misc.*
import simulation.physics.castInteractableRay

fun updateMoney(deck: Deck, events: Events, character: Id, money: Int): Int {
  val rewards = events
      .filterIsInstance<Command>()
      .filter { it.type == ContractCommands.payAgent }
      .sumBy { it.value as? Int ?: 0 }

  val cost = getPurchaseCost(deck, events, character)
  return money + rewards - cost // + moneyFromItems
}

fun updateInteractingWith(deck: Deck, character: Id, commands: Commands, interactingWith: Id?): Id? =
    if (commands.any { it.type == CharacterCommands.interactWith })
      deck.characters[character]!!.canInteractWith
    else if (commands.any { it.type == CharacterCommands.stopInteracting } ||
        (interactingWith != null && !deck.interactables.containsKey(interactingWith)))
      null
    else
      interactingWith

fun updateAccessoryPoints(events: Events, character: Character): Int {
  return if (character.faction == Factions.misfits) {
    val victoryKeyEventPlacementCount = events.filterIsInstance<PlaceVictoryKeyEvent>().count()
    val removal = if (character.accessoryPoints > 0 && character.accessoryOptions == null)
      -1
    else
      0
    character.accessoryPoints + victoryKeyEventPlacementCount + removal
  } else
    character.accessoryPoints
}

fun updateAccessoryOptions(definitions: Definitions, dice: Dice, deck: Deck, events: Events, actor: Id, character: Character): AccessoryOptions? {
  return if (character.faction == Factions.misfits)
    if (character.accessoryPoints > 0 && character.accessoryOptions == null)
      newAccessoryChoice(definitions, dice, deck, actor)
    else if (events.filterIsInstance<ChooseImprovedAccessory>().any { it.actor == actor })
      null
    else
      character.accessoryOptions
  else
    character.accessoryOptions
}

fun getShadowSpirit(deck: Deck, actor: Id): Id? {
  val playerRecord = deck.players[actor]
  val rig = playerRecord?.rig
  return if (rig != null && rig != actor)
    rig
  else
    null
}

fun updateCharacter(definitions: Definitions, dice: Dice, deck: Deck, bulletState: BulletState, actor: Id, character: Character,
                    commands: Commands, events: Events): Character {
  val destructible = deck.destructibles[actor]!!
  val body = deck.bodies[actor]!!
  val position = body.position
  val isAlive = isAlive(destructible.health, character, position)
  val canInteractWith = if (deck.players.containsKey(actor))
    castInteractableRay(bulletState.dynamicsWorld, deck, actor)
  else
    null

  val shadowSpirit = getShadowSpirit(deck, actor)
  val shadowSpiritBody = deck.bodies[shadowSpirit]

  val mainEnergyExpense = getEnergyExpense(standardEnergyRates, 1, toInt1000(body.velocity.length()))
  val shadowEnergyExpense = if (shadowSpiritBody != null)
    getEnergyExpense(shadowSpiritEnergyRates, 1, toInt1000(shadowSpiritBody.velocity.length()))
  else
    0

  val energyAccumulator = character.energyAccumulator - mainEnergyExpense - shadowEnergyExpense
  val energyAccumulation = getRoundedAccumulation(energyAccumulator)

  return character.copy(
      isAlive = isAlive,
      isInfinitelyFalling = isInfinitelyFalling(position),
      canInteractWith = canInteractWith,
      interactingWith = updateInteractingWith(deck, actor, commands, character.interactingWith),
      money = updateMoney(deck, events, actor, character.money),
      accessoryPoints = updateAccessoryPoints(events, character),
      accessoryOptions = updateAccessoryOptions(definitions, dice, deck, events, actor, character),
      energy = modifyResourceWithEvents(events, actor, ResourceTypes.energy, character.energy, destructible.health, energyAccumulation),
      energyAccumulator = energyAccumulator - energyAccumulation * highIntScale,
      availableContracts = updateAvailableContracts(commands, character.availableContracts),
      utilityItem = updateUtilityItem(definitions, deck, commands, actor, character),
  )
}

fun updateCharacter(definitions: Definitions, dice: Dice, deck: Deck, bulletState: BulletState, events: Events): (Id, Character) -> Character = { id, character ->
  val commands = events.filterIsInstance<Command>()
  val characterCommands = pipe2(commands, listOf(
      { c -> if (deck.characters[id]!!.isAlive) c else listOf() },
      { c -> c.filter { it.target == id } }
  ))

  updateCharacter(definitions, dice, deck, bulletState, id, character, characterCommands, events)
}
