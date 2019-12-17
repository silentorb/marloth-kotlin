package simulation.entities

import silentorb.mythic.ent.Id
import silentorb.mythic.ent.pipe2
import marloth.scenery.enums.ResourceId
import marloth.scenery.enums.Sounds
import silentorb.mythic.happenings.*
import silentorb.mythic.physics.BulletState
import simulation.combat.DamageMultipliers
import simulation.happenings.PurchaseEvent
import simulation.happenings.TakeItemEvent
import simulation.main.Deck
import simulation.misc.*
import simulation.physics.castInteractableRay
import simulation.updating.simulationDelta

data class CharacterDefinition(
    val health: Int,
    val maxSpeed: Float,
    val accessories: List<AccessoryName>,
    val depictionType: DepictionType,
    val deathSound: Sounds,
    val ambientSounds: List<Sounds> = listOf(),
    val damageMultipliers: DamageMultipliers = mapOf()
)

data class Character(
    val definition: CharacterDefinition,
    val faction: Id,
    val sanity: ResourceContainer,
    val isAlive: Boolean = true,
    val activeAccessory: Id? = null,
    val canInteractWith: Id? = null,
    val interactingWith: Id? = null,
    val money: Int = 0
)

// Currently this is such a simple function because it will likely get more complicated and I want to ensure
// everything is already routing through a single point before things get more complicated.
fun isAlive(health: Int): Boolean =
    health > 0

val equipCommandSlots: Map<CommandName, Int> = listOf(
    CommonCharacterCommands.equipSlot0,
    CommonCharacterCommands.equipSlot1,
    CommonCharacterCommands.equipSlot2,
    CommonCharacterCommands.equipSlot3
).mapIndexed { index, commandType -> Pair(commandType, index) }
    .associate { it }

fun updateEquippedItem(deck: Deck, id: Id, activeAccessory: Id?, commands: Commands): Id? {
  val slot = commands
      .mapNotNull { equipCommandSlots[it.type] }
      .firstOrNull()

  return if (slot != null) {
    val itemId = getItemInSlot(deck, id, slot)
    if (itemId == activeAccessory)
      null
    else
      itemId
  } else if (activeAccessory == null) {
    val result = getTargetAttachments(deck, id).entries.firstOrNull { deck.actions.keys.contains(it.key) }?.key
    result
  } else
    activeAccessory
}

fun getPurchaseCost(deck: Deck, events: Events, character: Id): Int {
  val purchases = events.filterIsInstance<PurchaseEvent>()
      .filter { it.customer == character }

  return purchases.map { purchase ->
    val ware = deck.wares[purchase.ware]!!
    ware.price
  }
      .sum()
}

fun getMoneyFromTakenItems(deck: Deck, events: Events, character: Id): Int {
  val takes = events.filterIsInstance<TakeItemEvent>().filter { it.actor == character }.map { it.item }
  val moneyTakes = deck.resources.filterKeys { takes.contains(it) }
  return moneyTakes
      .mapNotNull { it.value.values[ResourceId.money] }
      .sum()
}

fun updateMoney(deck: Deck, events: Events, character: Id, money: Int): Int {
  val moneyFromItems = getMoneyFromTakenItems(deck, events, character)
  val cost = getPurchaseCost(deck, events, character)
  return money - cost + moneyFromItems
}

fun updateInteractingWith(deck: Deck, character: Id, commands: Commands, interactingWith: Id?): Id? =
    if (commands.any { it.type == CommonCharacterCommands.interactPrimary })
      deck.characters[character]!!.canInteractWith
    else if (commands.any { it.type == CommonCharacterCommands.stopInteracting } ||
        (interactingWith != null && !deck.interactables.containsKey(interactingWith)))
      null
    else
      interactingWith

fun updateCharacter(deck: Deck, bulletState: BulletState, id: Id, character: Character,
                    commands: Commands, events: Events): Character {
  val destructible = deck.destructibles[id]!!
  val isAlive = isAlive(destructible.health.value)
  val canInteractWith = if (deck.players.containsKey(id))
    castInteractableRay(bulletState.dynamicsWorld, deck, id)
  else
    null

  return character.copy(
      isAlive = isAlive,
      activeAccessory = updateEquippedItem(deck, id, character.activeAccessory, commands),
      canInteractWith = canInteractWith,
      interactingWith = updateInteractingWith(deck, id, commands, character.interactingWith),
      money = updateMoney(deck, events, id, character.money)
  )
}

fun updateCharacter(deck: Deck, bulletState: BulletState, events: Events): (Id, Character) -> Character = { id, character ->
  val delta = simulationDelta
  val commands = events.filterIsInstance<CharacterCommand>()
  if (commands.any()) {
    val k = 0
  }

  val characterCommands = pipe2(commands, listOf(
      { c -> if (deck.characters[id]!!.isAlive) c else listOf() },
      { c -> c.filter { it.target == id } }
  ))

  updateCharacter(deck, bulletState, id, character, characterCommands, events)
}
