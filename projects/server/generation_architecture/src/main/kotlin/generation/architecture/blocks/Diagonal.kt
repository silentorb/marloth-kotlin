package generation.architecture.blocks

import generation.architecture.connecting.*
import generation.architecture.matrical.*
import generation.general.Block
import generation.general.endpoint
import simulation.misc.CellAttribute

val diagonalCornerBlock: TieredBlock = { level ->
  val openRequired = levelSides[level].openRequired
  Block(
      name = "diagonalCorner$level",
      sides = sides(
          east = openRequired,
          north = openRequired,
          west = endpoint,
          south = endpoint
      ),
      attributes = setOf(CellAttribute.categoryDiagonal, CellAttribute.traversable)
  )
}


