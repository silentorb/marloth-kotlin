package junk_simulation.logic

import junk_simulation.Ability
import junk_simulation.Element
import junk_simulation.Resource
import junk_simulation.SimpleResource

private val baseResourcePoints = 4
private val baseResourcePointsPerLevel = 1
private val baseResourcePointsPerLevelPerElement = 1

fun calculateResourcePoints(characterLevel: Int, resourceCount: Int): Int {
  val levelMultiplier = baseResourcePointsPerLevel + baseResourcePointsPerLevelPerElement * resourceCount
  return baseResourcePoints + characterLevel * levelMultiplier
}

fun allocateResources(characterLevel: Int, abilities: List<Ability>): List<SimpleResource> {
  val totalRelativePoints = abilities.sumBy { it.type.purchaseCost * it.level }
  val distributed = abilities.map { Pair(it.type, it.type.purchaseCost * it.level / it.type.usageCost.size) }

  val groups = Element.values().map { element ->
    Pair(element, distributed
        .filter { it.first.usageCost.containsKey(element) }
        .sumBy { it.second }
    )
  }
      .filter { it.second > 0 }

  val characterResourcePoints = calculateResourcePoints(characterLevel, groups.size)

  return groups
      .map {
        val max = it.second * characterResourcePoints / totalRelativePoints
        SimpleResource(it.first, max)
      }
      .sortedBy { it.max }
}

val convertSimpleResource = { simple: SimpleResource ->
  Resource(
      element = simple.element,
      max = simple.max,
      value = simple.max
  )
}