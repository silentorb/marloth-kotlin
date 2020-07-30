package generation.architecture.blocks

import generation.architecture.definition.levelSides
import generation.architecture.engine.squareOffsets
import generation.architecture.matrical.TieredBlock
import generation.architecture.matrical.getLevelHeight
import generation.architecture.matrical.sides
import generation.general.Block
import silentorb.mythic.spatial.Vector3
import simulation.misc.CellAttribute

val squareRoom: TieredBlock = { level ->
  val open = levelSides[level].open
  Block(
      name = "halfStepRoom$level",
      sides = sides(
          east = open,
          north = open,
          west = open,
          south = open
      ),
      attributes = setOf(CellAttribute.traversable),
      slots = squareOffsets(2).map { it + Vector3(0f, 0f, getLevelHeight(level)) }
  )
}
