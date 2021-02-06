package marloth.definition.misc

import silentorb.mythic.ent.reflectProperties

object BlockSides {
  val closed = "closed"
  val open = "open"
  val slopeLeft = "slopeLeft"
  val slopeRight = "slopeRight"
}

val blockSides = reflectProperties<String>(BlockSides)

val traversibleBlockSides = setOf(
    BlockSides.open,
)
