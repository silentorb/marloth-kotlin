package simulation.characters

import marloth.scenery.enums.CharacterCommands
import marloth.scenery.enums.ResourceId
import marloth.scenery.enums.Text
import silentorb.mythic.accessorize.AccessoryName
import silentorb.mythic.audio.SoundName
import silentorb.mythic.aura.SoundType
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.ent.pipe2
import silentorb.mythic.happenings.CharacterCommand
import silentorb.mythic.happenings.Commands
import silentorb.mythic.happenings.Events
import silentorb.mythic.physics.BulletState
import silentorb.mythic.spatial.Vector3
import simulation.combat.general.DamageMultipliers
import simulation.combat.general.ResourceContainer
import simulation.entities.DepictionType
import simulation.happenings.PurchaseEvent
import simulation.happenings.TakeItemEvent
import simulation.main.Deck
import simulation.physics.castInteractableRay
import simulation.updating.simulationDelta

const val fieldOfView360 = -1f

typealias ProfessionId = String

data class CharacterDefinition(
    val name: Text,
    val health: Int,
    val maxSpeed: Float,
    val accessories: List<AccessoryName>,
    val depictionType: DepictionType,
    val deathSound: SoundName?,
    val ambientSounds: List<SoundType> = listOf(),
    val damageMultipliers: DamageMultipliers = mapOf(),
    val fieldOfView: Float = 0.5f // Only used for AI. Dot product: 1 is no vision, -1 is 360 degree vision
)

data class Character(
    val profession: ProfessionId,
    val definition: CharacterDefinition,
    val faction: Id,
    val sanity: ResourceContainer,
    val isAlive: Boolean,
    val activeAccessory: Id?,
    val canInteractWith: Id? = null,
    val interactingWith: Id? = null,
    val isInfinitelyFalling: Boolean = false,
    val money: Int = 0
)

fun isInfinitelyFalling(position: Vector3): Boolean =
    position.z < -100f

// Currently this is such a simple function because it will likely get more complicated and I want to ensure
// everything is already routing through a single point before things get more complicated.
fun isAlive(health: Int, position: Vector3): Boolean =
    health > 0 && !isInfinitelyFalling(position)

//val equipCommandSlots: Map<CommandName, Int> = listOf(
//    CharacterCommands.equipSlot0,
//    CharacterCommands.equipSlot1,
//    CharacterCommands.equipSlot2,
//    CharacterCommands.equipSlot3
//).mapIndexed { index, commandType -> Pair(commandType, index) }
//    .associate { it }
//
//fun updateEquippedItem(deck: Deck, id: Id, activeAccessory: Id?, commands: Commands): Id? {
//  val slot = commands
//      .mapNotNull { equipCommandSlots[it.type] }
//      .firstOrNull()
//
//  return if (slot != null) {
//    val itemId = getItemInSlot(deck, id, slot)
//    if (itemId == activeAccessory)
//      null
//    else
//      itemId
//  } else if (activeAccessory == null) {
//    val result = getTargetAttachments(deck, id).entries.firstOrNull { deck.actions.keys.contains(it.key) }?.key
//    result
//  } else
//    activeAccessory
//}

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
      .mapNotNull { it.value.values[ResourceId.money.name] }
      .sum()
}

fun updateMoney(deck: Deck, events: Events, character: Id, money: Int): Int {
  val moneyFromItems = getMoneyFromTakenItems(deck, events, character)
  val cost = getPurchaseCost(deck, events, character)
  return money - cost + moneyFromItems
}

fun updateInteractingWith(deck: Deck, character: Id, commands: Commands, interactingWith: Id?): Id? =
    if (commands.any { it.type == CharacterCommands.interactPrimary })
      deck.characters[character]!!.canInteractWith
    else if (commands.any { it.type == CharacterCommands.stopInteracting } ||
        (interactingWith != null && !deck.interactables.containsKey(interactingWith)))
      null
    else
      interactingWith

fun updateCharacter(deck: Deck, bulletState: BulletState, id: Id, character: Character,
                    commands: Commands, events: Events): Character {
  val destructible = deck.destructibles[id]!!
  val position = deck.bodies[id]!!.position
  val isAlive = isAlive(destructible.health.value, position)
  val canInteractWith = if (deck.players.containsKey(id))
    castInteractableRay(bulletState.dynamicsWorld, deck, id)
  else
    null

  return character.copy(
      isAlive = isAlive,
      isInfinitelyFalling = isInfinitelyFalling(position),
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

fun isEnemy(characters: Table<Character>, faction: Id): (Id) -> Boolean = { id ->
  characters[id]!!.faction != faction
}

fun isAlly(characters: Table<Character>, faction: Id): (Id) -> Boolean = { id ->
  characters[id]!!.faction == faction
}
