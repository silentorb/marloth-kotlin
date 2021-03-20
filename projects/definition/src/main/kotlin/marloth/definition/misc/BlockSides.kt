package marloth.definition.misc

import silentorb.mythic.ent.reflectProperties

object BlockSides {
  val open = "open"
  val partialOpen = "partialOpen"
  val slopeLeftHalf = "slopeLeftHalf"
  val slopeRightHalf = "slopeRightHalf"
  val slopeLeftQuarter = "slopeLeftQuarter"
  val slopeRightQuarter = "slopeRightQuarter"
}


val traversableBlockSides = setOf(
    BlockSides.open,
)

val sideGroups: Map<String, Set<String>> = mapOf(
    "anyOpen" to setOf(
        BlockSides.open,
        BlockSides.partialOpen
    ),
)

val blockSides = reflectProperties<String>(BlockSides) + sideGroups.keys
