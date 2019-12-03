package marloth.generation.population

import generation.abstracted.distributeToSlots
import generation.abstracted.normalizeRanges
import generation.architecture.misc.GenerationConfig
import marloth.definition.templates.defaultWares
import marloth.definition.templates.newMerchant
import mythic.ent.Id
import randomly.Dice
import scenery.enums.ModifierId
import simulation.main.Hand
import simulation.misc.Node
import simulation.misc.NodeAttribute
import simulation.misc.Realm
import simulation.misc.getRooms


enum class Occupant {
  coldCloud,
  fireCloud,
  enemy,
  merchant,
  none,
  poisonCloud,
  treasureChest
}

typealias DistributionMap = Map<Occupant, Int>

typealias OccupantToHand = (Node, Occupant) -> Hand?

fun occupantPopulator(config: GenerationConfig): OccupantToHand = { node, occupant ->
  when (occupant) {
    Occupant.coldCloud -> placeBuffCloud(node, ModifierId.damageChilled)
    Occupant.fireCloud -> placeBuffCloud(node, ModifierId.damageBurning)
    Occupant.enemy -> if (config.includeEnemies) placeEnemy(config.definitions, node) else null
    Occupant.merchant -> newMerchant(config.definitions, node.position, defaultWares)
    Occupant.none -> null
    Occupant.poisonCloud -> placeBuffCloud(node, ModifierId.damagePoisoned)
    Occupant.treasureChest -> placeTreasureChest(config.meshes, node, 10)
  }
}

fun damageCloudsDistributions(dice: Dice, totalWeight: Int): DistributionMap {
  val cloudTypes = listOf(
      Occupant.coldCloud,
      Occupant.fireCloud,
      Occupant.poisonCloud
  )

  val initialWeights = cloudTypes
      .map { Pair(it, dice.getInt(0, 100)) }
      .associate { it }

  return normalizeRanges(totalWeight, initialWeights)
}

fun scalingDistributions(dice: Dice): DistributionMap = mapOf(
    Occupant.enemy to 0,
    Occupant.merchant to 0,
    Occupant.none to 30,
    Occupant.treasureChest to 20
).plus(damageCloudsDistributions(dice, 0))
//).plus(damageCloudsDistributions(dice, 10))

fun fixedDistributions(): DistributionMap = mapOf(
    Occupant.enemy to 1,
    Occupant.merchant to 0,
    Occupant.none to 0,
    Occupant.treasureChest to 0
)

fun populateRooms(occupantToHand: OccupantToHand, dice: Dice, realm: Realm, playerNode: Id): List<Hand> {
  if (System.getenv("NO_OBJECTS") != null)
    return listOf()

  val rooms = getRooms(realm).filter { it.id != playerNode && it.attributes.contains(NodeAttribute.fullFloor) }
  val scaling = scalingDistributions(dice)
  val fixed = fixedDistributions()
  val occupants = distributeToSlots(dice, rooms.size, scaling, fixed)
  val hands = rooms
      .zip(occupants, occupantToHand)
      .filterNotNull()

  return hands
}
