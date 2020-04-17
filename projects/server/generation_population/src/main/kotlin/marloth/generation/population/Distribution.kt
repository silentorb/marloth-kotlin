package marloth.generation.population

import generation.abstracted.distributeToSlots
import generation.abstracted.normalizeRanges
import generation.architecture.misc.GenerationConfig
import marloth.definition.data.creatures
import marloth.definition.templates.defaultWares
import marloth.definition.templates.newMerchant
import marloth.scenery.enums.ModifierId
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.debugging.getDebugString
import silentorb.mythic.ent.IdSource
import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i
import simulation.entities.CharacterDefinition
import simulation.main.IdHand
import simulation.misc.CellAttribute
import simulation.misc.Realm
import simulation.misc.absoluteCellPosition
import simulation.misc.cellLength

enum class DistributionGroup {
  cloud,
  merchant,
  monster,
  none,
  treasureChest
}

typealias DistributionMap = Map<DistributionGroup, Int>

//fun damageCloudsDistributions(dice: Dice, totalWeight: Int): DistributionMap {
//  val cloudTypes = listOf(
//      ModifierId.damageChilled,
//      ModifierId.damageBurning,
//      ModifierId.damagePoisoned
//  )
//
//  val initialWeights = cloudTypes
//      .map { Pair(it, dice.getInt(0, 100)) }
//      .associate { it }
//
//  return normalizeRanges(totalWeight, initialWeights)
//}

fun enemyDistributions() = mapOf(
    creatures.hogMan to 5,
    creatures.sentinel to 20
)

fun scalingDistributions(dice: Dice): DistributionMap = mapOf(
    DistributionGroup.none to 10,
    DistributionGroup.monster to if (getDebugBoolean("SINGLE_MONSTER")) 0 else 2

)
//) + damageCloudsDistributions(dice, 10)

fun fixedDistributions(): DistributionMap =
    if (getDebugBoolean("SINGLE_MONSTER"))
      mapOf(
          DistributionGroup.monster to 1
      )
    else
      mapOf(
          DistributionGroup.monster to 2
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

fun getGroupDistributions(dice: Dice, realm: Realm): Map<DistributionGroup, List<Vector3>> {
  if (getDebugString("NO_OBJECTS") != null)
    return mapOf()

  val locations = realm.grid.cells
      .filter { supportsPopulation(it.value.attributes) }
      .keys
      .flatMap(partitionCell(partitionOffsets(2)))

  val scaling = scalingDistributions(dice)
  val fixed = fixedDistributions()
  val occupants = distributeToSlots(dice, locations.size, scaling, fixed)
  val pairs = locations
      .zip(occupants) { location, occupant -> Pair(location, occupant) }

  return DistributionGroup.values()
      .associate { group ->
        Pair(group, pairs.filter { it.second == group }.map { it.first })
      }
}

fun populateMonsters(config: GenerationConfig, locations: List<Vector3>, nextId: IdSource, dice: Dice): List<IdHand> {
  val distributions = distributeToSlots(dice, locations.size, enemyDistributions(), mapOf())
  return locations
      .zip(distributions) { location, definition ->
        placeEnemy(nextId, config.definitions, location, definition)
      }
      .flatten()
}

fun populateRooms(config: GenerationConfig, nextId: IdSource, dice: Dice, realm: Realm): List<IdHand> {
  val groupDistributions = getGroupDistributions(dice, realm)
  val monsters = if (config.includeEnemies)
    populateMonsters(config, groupDistributions[DistributionGroup.monster] ?: listOf(), nextId, dice)
  else
    listOf()

  return monsters
}
