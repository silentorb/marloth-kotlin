package marloth.generation.population

import generation.abstracted.distributeToSlots
import generation.abstracted.normalizeRanges
import generation.architecture.misc.GenerationConfig
import marloth.definition.templates.defaultWares
import marloth.definition.templates.newMerchant
import silentorb.mythic.debugging.getDebugString
import silentorb.mythic.ent.IdSource
import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i
import simulation.main.IdHand
import simulation.misc.CellAttribute
import simulation.misc.Realm
import simulation.misc.absoluteCellPosition
import simulation.misc.cellLength

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

typealias OccupantToHand = (Vector3, Occupant) -> List<IdHand>?

fun occupantPopulator(config: GenerationConfig, nextId: IdSource): OccupantToHand = { cell, occupant ->
  when (occupant) {
//    Occupant.coldCloud -> placeBuffCloud(node, ModifierId.damageChilled)
//    Occupant.fireCloud -> placeBuffCloud(node, ModifierId.damageBurning)
    Occupant.enemy -> if (config.includeEnemies) placeEnemy(nextId, config.definitions, cell) else null
    Occupant.merchant -> newMerchant(nextId, config.definitions, cell, defaultWares)
    Occupant.none -> null
//    Occupant.poisonCloud -> placeBuffCloud(node, ModifierId.damagePoisoned)
//    Occupant.treasureChest -> placeTreasureChest(config.meshes, node, 10)
    else -> {
      println("Need to eventually update occupantPopulator")
      null
    }
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
    Occupant.enemy to 20,
    Occupant.merchant to 0,
    Occupant.none to 10,
    Occupant.treasureChest to 0
).plus(damageCloudsDistributions(dice, 0))
//).plus(damageCloudsDistributions(dice, 10))

fun fixedDistributions(): DistributionMap = mapOf(
    Occupant.enemy to 1,
    Occupant.merchant to 0,
    Occupant.none to 0,
    Occupant.treasureChest to 0
)

fun supportsPopulation(attributes: Set<CellAttribute>): Boolean =
    attributes.contains(CellAttribute.fullFloor)
        && !attributes.contains(CellAttribute.home)
        && !attributes.contains(CellAttribute.exit)

fun partitionOffsets(resolution: Int): List<Vector3> {
  val step = cellLength / resolution
  val start = step / 2f

  return (0 until resolution).flatMap { y ->
    (0 until resolution).map { x ->
      Vector3(start + step * x, start + step * y, 1f)
    }
  }
}

fun partitionCell(offsets: List<Vector3>): (Vector3i) -> List<Vector3> = { cell ->
  val absolute = absoluteCellPosition(cell)
  offsets
      .map { offset ->
        absolute + offset
      }
}

fun populateRooms(occupantToHand: OccupantToHand, dice: Dice, realm: Realm): List<IdHand> {
  if (getDebugString("NO_OBJECTS") != null)
    return listOf()

  val locations = realm.grid.cells
      .filter { supportsPopulation(it.value.attributes) }
      .keys
      .flatMap(partitionCell(partitionOffsets(2)))

  val scaling = scalingDistributions(dice)
  val fixed = fixedDistributions()
  val occupants = distributeToSlots(dice, locations.size, scaling, fixed)
  val hands = locations
      .zip(occupants, occupantToHand)
      .filterNotNull()
      .flatten()

  return hands
}
