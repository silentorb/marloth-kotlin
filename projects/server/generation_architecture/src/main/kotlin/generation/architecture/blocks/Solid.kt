package generation.architecture.blocks

import generation.architecture.connecting.Sides
import generation.architecture.matrical.TieredBlock
import generation.architecture.matrical.sides
import generation.general.Block
import generation.general.endpoint

val solidTieredBlock: TieredBlock = { level ->
  val solid = Sides.solid
  val solidRequired = Sides.solidRequired
  Block(
      name = "solidBlock$level",
      sides = sides(
          up = solidRequired,
          down = endpoint,
          east = solid,
          north = solid,
          west = solid,
          south = solid
      ),
      attributes = setOf()
  )
}

fun solidBlockSides() = sides(
    up = Sides.solidRequired,
    east = Sides.solid,
    north = Sides.solid,
    west = Sides.solid,
    south = Sides.solid
)

fun solidBlock() = Block(
    name = "solidBlock",
    sides = solidBlockSides()
)

fun solidDiagonal() =
    Block(
        name = "solidDiagonal",
        sides = sides(
            up = Sides.solidRequired,
            east = Sides.solid,
            north = Sides.solid,
            west = endpoint,
            south = endpoint
        )
)
