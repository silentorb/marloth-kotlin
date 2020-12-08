package simulation.characters

import marloth.scenery.enums.CharacterCommands
import marloth.scenery.enums.SoundId
import marloth.scenery.enums.Text
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
import simulation.accessorize.AccessoryName
import simulation.accessorize.ChooseImprovedAccessory
import simulation.accessorize.newAccessoryChoice
import simulation.combat.general.DamageMultipliers
import simulation.combat.general.ResourceContainer
import simulation.entities.*
import simulation.happenings.PurchaseEvent
import simulation.main.Deck
import simulation.misc.*
import simulation.physics.castInteractableRay

const val fieldOfView360 = -1f
const val maxCharacterLevel = 3

typealias ProfessionId = String

data class CharacterDefinition(
    val name: Text,
    val level: Int = 1,
    val health: Int = 200,
    val speed: Float = 12f,
    val accessories: List<AccessoryName> = listOf(),
    val depictionType: DepictionType,
    val deathSound: SoundName? = SoundId.girlScream,
    val ambientSounds: List<SoundType> = listOf(),
    val damageMultipliers: DamageMultipliers = mapOf(),
    val money: Int = 0,
    val fieldOfView: Float = 0.5f, // Only used for AI. Dot product: 1 is no vision, -1 is 360 degree vision
    val wares: List<Ware> = listOf(),
    val availableContracts: List<ContractDefinition> = listOf(),
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
    val definition: CharacterDefinition,
    val faction: Id,
    val sanity: ResourceContainer = ResourceContainer(100),
    val isAlive: Boolean,
    val canInteractWith: Id? = null,
    val interactingWith: Id? = null,
    val isInfinitelyFalling: Boolean = false,
    val accessoryPoints: Int = 0,
    val accessoryOptions: AccessoryOptions? = null,
    val money: Int = 0,
    val nourishment: HighInt = highIntScale,
    val wares: Map<Id, Ware> = mapOf(),
    val availableContracts: Map<Id, ContractDefinition> = mapOf(),
)

data class ModifyLevelEvent(
    val actor: Id,
    val offset: Int
)

fun getFaction(deck: Deck, actor: Id): Id? =
    deck.characters[actor]?.faction

fun isInfinitelyFalling(position: Vector3): Boolean =
    position.z < -100f

fun isAlive(health: Int, nourishment: HighInt, position: Vector3): Boolean =
    health > 0 &&
        nourishment > 0 &&
        !isInfinitelyFalling(position)

fun getPurchaseCost(deck: Deck, events: Events, character: Id): Int {
  val purchases = events.filterIsInstance<PurchaseEvent>()
      .filter { it.customer == character }

  return purchases
      .mapNotNull { purchase ->
        val wares = deck.characters[purchase.merchant]?.wares
        assert(wares != null)
        wares!![purchase.ware]?.price
      }
      .sum()
}

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

fun updateCharacter(definitions: Definitions, dice: Dice, deck: Deck, bulletState: BulletState, actor: Id, character: Character,
                    commands: Commands, events: Events): Character {
  val destructible = deck.destructibles[actor]!!
  val body = deck.bodies[actor]!!
  val position = body.position
  val isAlive = isAlive(destructible.health.value, character.nourishment, position)
  val canInteractWith = if (deck.players.containsKey(actor))
    castInteractableRay(bulletState.dynamicsWorld, deck, actor)
  else
    null

  val nourishmentAdjustment = getNourishmentEventsAdjustment(definitions, deck, actor, events)

  return character.copy(
      isAlive = isAlive,
      isInfinitelyFalling = isInfinitelyFalling(position),
      canInteractWith = canInteractWith,
      interactingWith = updateInteractingWith(deck, actor, commands, character.interactingWith),
      money = updateMoney(deck, events, actor, character.money),
//      definition = updateCharacterProfession(definitions, actor, events, character.definition),
      accessoryPoints = updateAccessoryPoints(events, character),
      accessoryOptions = updateAccessoryOptions(definitions, dice, deck, events, actor, character),
      nourishment = updateNourishment(1, character, nourishmentAdjustment, toInt1000(body.velocity.length())),
      availableContracts = updateAvailableContracts(commands, character.availableContracts)
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
