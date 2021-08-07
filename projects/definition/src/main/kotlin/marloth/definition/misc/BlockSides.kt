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
}

val nonTraversableBlockSides = setOf(
    BlockSides.aerialOpen,
    BlockSides.closed,
)

val anyOpen = setOf(
    BlockSides.open,
    BlockSides.partialOpen
)

val sideGroups: Map<String, Set<String>> = mapOf(
    "anyOpen" to anyOpen,
    "anyOpenOrClosed" to anyOpen + BlockSides.closed,
)

val blockSides = reflectProperties<String>(BlockSides) + sideGroups.keys
