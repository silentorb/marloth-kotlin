package marloth.definition.misc

import silentorb.mythic.ent.reflectProperties

object BlockSides {
  val open = "open"
}

val blockSides = reflectProperties<String>(BlockSides)

val traversibleBlockSides = setOf(
    BlockSides.open,
)
