package generation.architecture.blocks

import generation.architecture.building.diagonalCorner
import generation.architecture.definition.Connector
import generation.architecture.definition.Sides
import generation.architecture.definition.levelConnectors
import generation.architecture.definition.preferredHorizontalClosed
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
  val upper = input.level
  val levelIndex = upper.index
  val halfStepOptionalOpen = input.sides.halfStepOptionalOpen
  listOf(
      diagonalCorner(
          "diagonalCorner$levelIndex",
          upper.height,
          sides(
              up = Sides.extraHeadroom,
              east = halfStepOptionalOpen,
              north = halfStepOptionalOpen,
              west = preferredHorizontalClosed(levelConnectors[levelIndex]),
              south = preferredHorizontalClosed(levelConnectors[levelIndex])
          ),
          tieredSquareFloorBuilder(upper)
      )
  )
}

fun octaveDiagonalCorner() =
    diagonalCorner(
        name = "diagonalCorner",
        height = 0f,
        sides = sides(
            east = Sides.broadOpen,
            north = Sides.broadOpen,
            west = preferredHorizontalClosed(Connector.open),
            south = preferredHorizontalClosed(Connector.open)
        ),
        fallback = singleCellRoomBuilder()
    )
