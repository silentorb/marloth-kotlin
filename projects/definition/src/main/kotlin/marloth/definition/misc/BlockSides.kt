package marloth.definition.misc

import silentorb.mythic.ent.reflectProperties

object BlockSides {
  val open = "open"
  val slopeLeft = "slopeLeft"
  val slopeRight = "slopeRight"
}

val blockSides = reflectProperties<String>(BlockSides)

val traversableBlockSides = setOf(
    BlockSides.open,
)
