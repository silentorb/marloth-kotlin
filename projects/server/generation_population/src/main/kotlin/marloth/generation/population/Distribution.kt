package marloth.generation.population

import generation.abstracted.distributeToSlots
import generation.architecture.misc.GenerationConfig
import marloth.definition.DistributionGroup
import marloth.definition.enemyDistributions
import marloth.definition.fixedDistributions
import marloth.definition.scalingDistributions
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.debugging.getDebugInt
import silentorb.mythic.debugging.getDebugString
import silentorb.mythic.ent.IdSource
import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i
import silentorb.mythic.spatial.toVector3
import simulation.main.IdHand
import simulation.misc.*
import kotlin.math.min

fun monsterLimit() = getDebugInt("MONSTER_LIMIT") ?: 1000

fun allocateVictoryKeyCells(cells: Set<Vector3i>, connections: ConnectionSet, home: Vector3i, victoryKeyCount: Int): List<Vector3i> {
  val homeVector3 = home.toVector3()
  // This is less an optimization and more to allow later logic that assumes victoryKeyCount is not zero
  if (victoryKeyCount == 0)
    return listOf()

  val (deadEnds, remaining) = cells
      .partition { cellConnections(connections, it).size == 1 }

  val usedDeadEnds = deadEnds
      .sortedByDescending { it.toVector3().distance(homeVector3) }
      .take(victoryKeyCount)

  // If there are not enough available cells this function will return an incomplete amount instead of throwing an error,
  // Unless there are no available victory key locations.
  val fallbackCount = min(victoryKeyCount - usedDeadEnds.size, remaining.size)
  assert(remaining.size >= fallbackCount)
  val fallbacks = remaining.map { Pair(it, it.toVector3().distance(home.toVector3())) }
      .sortedByDescending { it.second }
      .take(fallbackCount)
      .map { it.first }
//  val withFallBacks = (0 until fallbackCount)
//      .fold(Pair(usedDeadEnds, remaining)) { (used, options), _ ->
//        val next = options
//            .maxBy { option ->
//              val distance = used.map { it.toVector3().distance(option.toVector3()) }.maxBy { it }
//              distance ?: 0f
//            }
//        assert(next != null)
//        Pair(used + next!!, options - next)
//      }.first
  val result = usedDeadEnds + fallbacks
  if (result.none())
    throw Error("Could not find places for victory keys")

  assert(result.size == victoryKeyCount)

  return result
}

fun absoluteSlots(cells: CellMap): (Vector3i) -> List<Vector3> = { location ->
  val point = getCellPoint(location)
  cells[location]!!.slots.map { point + it }
}

// Only used for debug purposes
fun getAllSlots(grid: MapGrid): List<Vector3> {
  return grid.cells
      .filter { it.value.slots.any() }
      .keys
      .flatMap(absoluteSlots(grid.cells))
}

fun getGroupDistributions(dice: Dice, grid: MapGrid): Map<DistributionGroup, List<Vector3>> {
  if (getDebugString("NO_OBJECTS") != null)
    return mapOf()

  val availableCells = grid.cells
      .filter { it.value.slots.any() }
      .keys

  val home = grid.cells.filter { it.value.attributes.contains(CellAttribute.home) }.keys.firstOrNull()
  assert(home != null)

  val fixed = fixedDistributions()
  val scaling = scalingDistributions()
  assert(!scaling.containsKey(DistributionGroup.victoryKey))

  val victoryKeyCount = fixed[DistributionGroup.victoryKey] ?: 0
  val victoryKeyCells = allocateVictoryKeyCells(availableCells, grid.connections, home!!, victoryKeyCount)
  val victoryKeyLocations = victoryKeyCells.map { dice.takeOne(absoluteSlots(grid.cells)(it)) }

  val locations = availableCells
      .minus(victoryKeyCells)
      .flatMap(absoluteSlots(grid.cells))

  val occupants = distributeToSlots(dice, locations.size, scaling, fixed)
  val pairs = locations
      .zip(occupants) { location, occupant -> Pair(location, occupant) }

  return DistributionGroup.values()
      .associate { group ->
        Pair(group, pairs.filter { it.second == group }.map { it.first })
      }
      .plus(Pair(DistributionGroup.victoryKey, victoryKeyLocations))
}

fun populateMonsters(config: GenerationConfig, locations: List<Vector3>, nextId: IdSource, dice: Dice): List<IdHand> {
  println("Monster count: ${locations.size}")
  return if (locations.none())
    listOf()
  else {
    val distributions = distributeToSlots(dice, locations.size, enemyDistributions(), mapOf())
    locations
        .zip(distributions) { location, definition ->
          println("Monster location: $location")
          placeEnemy(nextId, config.definitions, location, definition)
        }
        .flatten()
  }
}

fun populateRooms(config: GenerationConfig, nextId: IdSource, dice: Dice, grid: MapGrid): List<IdHand> {
  return if (getDebugBoolean("DEBUG_CELL_SLOTS")) {
    getAllSlots(grid)
        .flatMap(newVictoryKeyPickup(nextId))
  } else {
    val groupDistributions = getGroupDistributions(dice, grid)
    val monsters = populateMonsters(config, (groupDistributions[DistributionGroup.monster]
        ?: listOf()).take(monsterLimit()), nextId, dice)

    val keys = groupDistributions
        .getOrElse(DistributionGroup.victoryKey) { listOf() }
        .flatMap(newVictoryKeyPickup(nextId))

    monsters + keys
  }
}
