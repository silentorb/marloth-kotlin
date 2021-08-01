package simulation.characters

import marloth.scenery.enums.Sounds
import marloth.scenery.enums.Text
import silentorb.mythic.audio.SoundName
import silentorb.mythic.aura.SoundType
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.happenings.Events
import silentorb.mythic.spatial.Vector3
import simulation.accessorize.AccessoryName
import simulation.combat.general.DamageMultipliers
import simulation.entities.ContractDefinition
import simulation.entities.DepictionType
import simulation.entities.Ware
import simulation.happenings.PurchaseEvent
import simulation.main.Deck

const val fieldOfView360 = -1f
const val maxCharacterLevel = 3

typealias ProfessionId = String

data class CharacterDefinition(
    val name: Text,
    val level: Int = 1,
    val health: Int = 100,
    val runSpeed: Float = 8f,
    val accessories: List<AccessoryName> = listOf(),
    val accessoryPool: List<AccessoryName> = listOf(),
    val depictionType: DepictionType,
    val deathSound: SoundName? = Sounds.girlScream,
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
    val sanity: Int = 100,
    val sanityAccumulator: Int = 0,
    val isAlive: Boolean,
    val canInteractWith: Id? = null,
    val interactingWith: Id? = null,
    val isInfinitelyFalling: Boolean = false,
    val accessoryPoints: Int = 0,
    val accessoryOptions: AccessoryOptions? = null,
    val money: Int = 0,
    val energy: Int,
    val energyAccumulator: Int = 0,
    val wares: Map<Id, Ware> = mapOf(),
    val availableContracts: Map<Id, ContractDefinition> = mapOf(),
    val utilityItem: Id? = null,
    val stepCounter: Float = 0f,
    val activity: String = CharacterActivity.nothing,
)

data class ModifyLevelEvent(
    val actor: Id,
    val offset: Int
)

fun getFaction(deck: Deck, actor: Id): Id? =
    deck.characters[actor]?.faction

fun isInfinitelyFalling(position: Vector3): Boolean =
    position.z < -100f

fun isAlive(health: Int, character: Character, position: Vector3): Boolean =
    health > 0 &&
//        character.nourishment > 0 &&
        character.energy > 0 &&
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

fun isEnemy(characters: Table<Character>, faction: Id): (Id) -> Boolean = { id ->
  characters[id]!!.faction != faction
}

fun isAlly(characters: Table<Character>, faction: Id): (Id) -> Boolean = { id ->
  characters[id]!!.faction == faction
}

fun isAliveOrNotACharacter(characters: Table<Character>, id: Id): Boolean {
  val character = characters[id]
  return character == null || character.isAlive
}
