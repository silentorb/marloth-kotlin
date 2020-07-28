package generation.architecture.blocks

import generation.architecture.building.diagonalCorner
import generation.architecture.definition.*
import generation.architecture.matrical.*
import generation.general.Block
import generation.general.SideMap
import simulation.misc.CellAttribute

fun diagonalCorner(name: String, height: Float, sides: SideMap, fallback: BiomedBuilder): BiomedBlockBuilder =
    BiomedBlockBuilder(
        block = Block(
            name = name,
            sides = sides,
            attributes = setOf(CellAttribute.categoryDiagonal, CellAttribute.traversable)
        ),
        builder = diagonalCorner(height, fallback)
    )

val diagonalCorner: MatrixBlockBuilder = { input ->
  val level = input.level
  val open = levelSides[level].open
  val preferredClosed = levelSides[level].preferredClosed
  listOf(
      diagonalCorner(
          "diagonalCorner$level",
          getLevelHeight(level),
          sides(
              east = open,
              north = open,
              west = preferredClosed,
              south = preferredClosed
          ),
          tieredCubeBuilder(level)
      )
  )
}
