package junk_simulation.logic

import junk_simulation.*

private val baseResourcePoints = 4
private val baseResourcePointsPerLevel = 1
private val baseResourcePointsPerLevelPerElement = 1

fun calculateResourcePoints(creatureLevel: Int, resourceCount: Int): Int {
  val levelMultiplier = baseResourcePointsPerLevel + baseResourcePointsPerLevelPerElement * resourceCount
  return baseResourcePoints + creatureLevel * levelMultiplier
}

fun allocateResources(creatureLevel: Int, abilities: List<Ability>): List<SimpleResource> {
  val totalRelativePoints = abilities.sumBy { it.type.purchaseCost }
  val distributed = abilities.map { Pair(it.type, it.type.purchaseCost / it.type.usageCost.size) }

  val groups = Element.values().map { element ->
    Pair(element, distributed
        .filter { it.first.usageCost.containsKey(element) }
        .sumBy { it.second }
    )
  }
      .filter { it.second > 0 }

  val creatureResourcePoints = calculateResourcePoints(creatureLevel, groups.size)

  return groups
      .map {
        val max = it.second * creatureResourcePoints / totalRelativePoints
        SimpleResource(it.first, max)
      }
      .sortedBy { it.max }
}

val convertSimpleResource = { simple: SimpleResource ->
  newResource(simple.element, simple.max)
}