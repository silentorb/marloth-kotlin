package generation.architecture.blocks

import generation.architecture.building.diagonalCornerBuilder
import generation.architecture.definition.*
import generation.architecture.matrical.*
import generation.general.Block
import generation.general.SideMap
import generation.general.endpoint
import silentorb.mythic.scenery.TextureName
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


