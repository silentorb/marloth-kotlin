package marloth.definition.misc

import silentorb.mythic.ent.reflectProperties

object BlockSides {
  val aerialOpen = "arialOpen"
  val open = "open"
  val closed = "closed"
  val partialOpen = "partialOpen"
  val slopeLeftHalf = "slopeLeftHalf"
  val slopeRightHalf = "slopeRightHalf"
  val slopeLeftQuarter = "slopeLeftQuarter"
  val slopeRightQuarter = "slopeRightQuarter"

  // City
  val street = "street"
  val streetIntersection = "streetIntersection"
  val buildingEntrance = "buildingEntrance"
}

val nonTraversableBlockSides = setOf(
    BlockSides.aerialOpen,
    BlockSides.closed,
    BlockSides.buildingEntrance,
)

val anyOpen = setOf(
    BlockSides.open,
    BlockSides.partialOpen,
)

val sideGroups: Map<String, Set<String>> = mapOf(
    "anyOpen" to anyOpen,
    "anyOpenOrClosed" to anyOpen + BlockSides.closed,
    "streetOrIntersection" to setOf(BlockSides.street, BlockSides.streetIntersection),
)

val blockSides = reflectProperties<String>(BlockSides) + sideGroups.keys
