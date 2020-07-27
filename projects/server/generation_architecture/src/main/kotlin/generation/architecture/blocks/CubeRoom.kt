package generation.architecture.blocks

import generation.architecture.building.floorMesh
import generation.architecture.definition.Sides
import generation.architecture.engine.squareOffsets
import generation.architecture.matrical.*
import generation.general.Block
import marloth.scenery.enums.MeshId
import silentorb.mythic.spatial.Vector3
import simulation.misc.CellAttribute

fun tieredSquareFloorBuilder(upper: Level) = mergeBuilders(
    floorMesh(MeshId.squareFloor, Vector3(0f, 0f, upper.height)),
    tieredWalls(getLowerLevel(upper))
)

val squareRoom: MatrixBlockBuilder = { input ->
  val level = input.level
  val levelIndex = level.index
  val halfStepOptionalOpen = input.sides.halfStepOptionalOpen
  listOf(
      BiomedBlockBuilder(
          block = Block(
              name = "halfStepRoom$levelIndex",
              sides = sides(
                  up = Sides.extraHeadroom,
                  east = halfStepOptionalOpen,
                  north = halfStepOptionalOpen,
                  west = halfStepOptionalOpen,
                  south = halfStepOptionalOpen
              ),
              attributes = setOf(CellAttribute.traversable),
              slots = squareOffsets(2).map { it + Vector3(0f, 0f, level.height) }
          ),
          builder = tieredSquareFloorBuilder(level)
      )
  )
}
