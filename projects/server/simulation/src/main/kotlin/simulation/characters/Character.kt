package simulation.characters

import marloth.scenery.enums.CharacterCommands
import marloth.scenery.enums.ResourceId
import marloth.scenery.enums.Text
import simulation.accessorize.AccessoryName
import simulation.accessorize.ChooseImprovedAccessory
import simulation.accessorize.newAccessoryChoice
import silentorb.mythic.audio.SoundName
import silentorb.mythic.aura.SoundType
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.ent.pipe2
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Commands
import silentorb.mythic.happenings.Events
import silentorb.mythic.physics.BulletState
import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.minMax
import simulation.combat.general.DamageMultipliers
import simulation.combat.general.ResourceContainer
import simulation.entities.DepictionType
import simulation.happenings.PurchaseEvent
import simulation.happenings.TakeItemEvent
import simulation.main.Deck
import simulation.misc.Definitions
import simulation.misc.PlaceVictoryKeyEvent
import simulation.misc.misfitFaction
import simulation.physics.castInteractableRay

const val fieldOfView360 = -1f
const val maxCharacterLevel = 3

typealias ProfessionId = String

data class CharacterDefinition(
    val name: Text,
    val level: Int = 1,
    val health: Int,
    val speed: Float,
    val accessories: List<AccessoryName>,
    val depictionType: DepictionType,
    val deathSound: SoundName?,
    val ambientSounds: List<SoundType> = listOf(),
    val damageMultipliers: DamageMultipliers = mapOf(),
    val fieldOfView: Float = 0.5f // Only used for AI. Dot product: 1 is no vision, -1 is 360 degree vision
)

enum class EquipmentSlot {
  automatic,
  attack,
  defense,
  mobility,
  utility
}

typealias Equipment = Map<EquipmentSlot, Id>

typealias AccessoryOptions = List<AccessoryName>

data class Character(
    val profession: ProfessionId,
    val faction: Id,
    val sanity: ResourceContainer = ResourceContainer(100),
    val isAlive: Boolean,
    val canInteractWith: Id? = null,
    val interactingWith: Id? = null,
    val isInfinitelyFalling: Boolean = false,
    val accessoryPoints: Int = 0,
    val accessoryOptions: AccessoryOptions? = null,
    val money: Int = 0
)

data class ModifyLevelEvent(
    val actor: Id,
    val offset: Int
)

fun getFaction(deck: Deck, actor: Id): Id? =
    deck.characters[actor]?.faction

fun isInfinitelyFalling(position: Vector3): Boolean =
    position.z < -100f

fun isAlive(health: Int, position: Vector3): Boolean =
    health > 0 && !isInfinitelyFalling(position)

fun getPurchaseCost(deck: Deck, events: Events, character: Id): Int {
  val purchases = events.filterIsInstance<PurchaseEvent>()
      .filter { it.customer == character }

  return purchases.map { purchase ->
    val ware = deck.wares[purchase.ware]!!
    ware.price
  }
      .sum()
}

//fun getMoneyFromTakenItems(deck: Deck, events: Events, character: Id): Int {
//  val takes = events.filterIsInstance<TakeItemEvent>().filter { it.actor == character }.map { it.item }
//  val moneyTakes = deck.resources.filterKeys { takes.contains(it) }
//  return moneyTakes
//      .mapNotNull { it.value.values[ResourceId.money.name] }
//      .sum()
//}

fun updateMoney(deck: Deck, events: Events, character: Id, money: Int): Int {
//  val moneyFromItems = getMoneyFromTakenItems(deck, events, character)
  val cost = getPurchaseCost(deck, events, character)
  return money - cost // + moneyFromItems
}

fun updateInteractingWith(deck: Deck, character: Id, commands: Commands, interactingWith: Id?): Id? =
    if (commands.any { it.type == CharacterCommands.interactPrimary })
      deck.characters[character]!!.canInteractWith
    else if (commands.any { it.type == CharacterCommands.stopInteracting } ||
        (interactingWith != null && !deck.interactables.containsKey(interactingWith)))
      null
    else
      interactingWith

fun updateCharacterProfession(definitions: Definitions, actor: Id, events: Events, profession: ProfessionId): ProfessionId {
  val modifyLevelEvents = events
      .filterIsInstance<ModifyLevelEvent>()
      .filter { it.actor == actor }

  return if (modifyLevelEvents.any()) {
    val definition = definitions.professions[profession]!!
    val level = minMax(0, maxCharacterLevel)(definition.level + modifyLevelEvents.sumBy { it.offset })
    profession.dropLast(1) + level
  } else
    profession
}

fun updateAccessoryPoints(events: Events, character: Character): Int {
  return if (character.faction == misfitFaction) {
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
  return if (character.faction == misfitFaction)
    if (character.accessoryPoints > 0 && character.accessoryOptions == null)
      newAccessoryChoice(definitions, dice, deck, actor)
    else if (events.filterIsInstance<ChooseImprovedAccessory>().any { it.actor == actor })
      null
    else
      character.accessoryOptions
  else
    character.accessoryOptions
}

fun updateCharacter(definitions: Definitions, dice: Dice, deck: Deck, bulletState: BulletState, actor: Id, character: Character,
                    commands: Commands, events: Events): Character {
  val destructible = deck.destructibles[actor]!!
  val position = deck.bodies[actor]!!.position
  val isAlive = isAlive(destructible.health.value, position)
  val canInteractWith = if (deck.players.containsKey(actor))
    castInteractableRay(bulletState.dynamicsWorld, deck, actor)
  else
    null

  return character.copy(
      isAlive = isAlive,
      isInfinitelyFalling = isInfinitelyFalling(position),
      canInteractWith = canInteractWith,
      interactingWith = updateInteractingWith(deck, actor, commands, character.interactingWith),
      money = updateMoney(deck, events, actor, character.money),
      profession = updateCharacterProfession(definitions, actor, events, character.profession),
      accessoryPoints = updateAccessoryPoints(events, character),
      accessoryOptions = updateAccessoryOptions(definitions, dice, deck, events, actor, character)
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

fun isEnemy(characters: Table<Character>, faction: Id): (Id) -> Boolean = { id ->
  characters[id]!!.faction != faction
}

fun isAlly(characters: Table<Character>, faction: Id): (Id) -> Boolean = { id ->
  characters[id]!!.faction == faction
}
