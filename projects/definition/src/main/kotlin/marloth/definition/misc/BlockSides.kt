package marloth.definition.misc

import silentorb.mythic.ent.reflectProperties

object BlockSides {
  val open = "open"
  val partialOpen = "partialOpen"
  val slopeLeft = "slopeLeft"
  val slopeRight = "slopeRight"
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
