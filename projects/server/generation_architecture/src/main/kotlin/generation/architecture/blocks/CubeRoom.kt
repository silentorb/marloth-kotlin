package generation.architecture.blocks

import generation.architecture.connecting.Sides
import generation.architecture.connecting.levelSides
import generation.architecture.engine.squareOffsets
import generation.architecture.matrical.TieredBlock
import generation.architecture.matrical.getLevelHeight
import generation.architecture.matrical.sides
import generation.general.Block
import generation.general.endpoint
import silentorb.mythic.spatial.Vector3
import simulation.misc.CellAttribute

val squareRoom: TieredBlock = { level ->
  val open = levelSides[level].open
  Block(
      name = "cubeRoom$level",
      sides = sides(
          east = open,
          north = open,
          west = open,
          south = open,
          down = Sides.solid,
          up = Sides.headroomVertical
      ),
      attributes = setOf(CellAttribute.traversable),
      slots = squareOffsets(2).map { it + Vector3(0f, 0f, getLevelHeight(level)) }
  )
}

val headroomBlock: Block = Block(
    name = "headroom",
    sides = sides(
        east = Sides.headroomHorizontal,
        north = Sides.headroomHorizontal,
        west = Sides.headroomHorizontal,
        south = Sides.headroomHorizontal,
        down = Sides.headroomVertical
    )
)
