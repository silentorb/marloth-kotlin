package generation.architecture.blocks

import generation.architecture.building.diagonalCorner
import generation.architecture.definition.*
import generation.architecture.matrical.*
import generation.general.Block
import generation.general.SideMap
import generation.general.endpoint
import simulation.misc.CellAttribute

fun diagonalCorner(name: String, height: Float, sides: SideMap): BiomedBlockBuilder =
    BiomedBlockBuilder(
        block = Block(
            name = name,
            sides = sides,
            attributes = setOf(CellAttribute.categoryDiagonal, CellAttribute.traversable)
        ),
        builder = diagonalCorner(height)
    )

val diagonalCorner: MatrixBlockBuilder = { input ->
  val level = input.level
  val openRequired = levelSides[level].openRequired
  listOf(
      diagonalCorner(
          "diagonalCorner$level",
          getLevelHeight(level),
          sides(
              east = openRequired,
              north = openRequired,
              west = endpoint,
              south = endpoint
          )
      )
  )
}
