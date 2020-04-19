package marloth.generation.population

import generation.abstracted.distributeToSlots
import generation.architecture.misc.GenerationConfig
import marloth.definition.DistributionGroup
import marloth.definition.enemyDistributions
import marloth.definition.fixedDistributions
import marloth.definition.scalingDistributions
import silentorb.mythic.debugging.getDebugInt
import silentorb.mythic.debugging.getDebugString
import silentorb.mythic.ent.IdSource
import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i
import simulation.main.IdHand
import simulation.misc.*

fun monsterLimit() = getDebugInt("MONSTER_LIMIT") ?: 1000

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

  val scaling = scalingDistributions()
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
  println("Monster count: ${locations.size}")
  return if (locations.none())
    listOf()
  else {
    val distributions = distributeToSlots(dice, locations.size, enemyDistributions(), mapOf())
    locations
        .zip(distributions) { location, definition ->
          placeEnemy(nextId, config.definitions, location, definition)
        }
        .flatten()
  }
}

fun populateRooms(config: GenerationConfig, nextId: IdSource, dice: Dice, realm: Realm): List<IdHand> {
  val groupDistributions = getGroupDistributions(dice, realm)
  val monsters = if (config.includeEnemies)
    populateMonsters(config, (groupDistributions[DistributionGroup.monster]
        ?: listOf()).take(monsterLimit()), nextId, dice)
  else
    listOf()

  val keys = groupDistributions[DistributionGroup.key]!!.flatMap(newVictoryKeyPickup(nextId))
  return monsters + keys
}
