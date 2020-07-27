package generation.architecture.blocks

import generation.architecture.building.cubeWallsWithFeatures
import generation.architecture.building.floorMesh
import generation.architecture.building.fullWallFeatures
import generation.architecture.definition.Sides
import generation.architecture.engine.squareOffsets
import generation.architecture.matrical.*
import generation.general.Block
import marloth.scenery.enums.MeshId
import silentorb.mythic.spatial.Vector3
import simulation.misc.BiomeName
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

fun singleCellRoomBuilder(): BiomedBuilder =
    mergeBuilders(
        floorMesh(MeshId.squareFloor),
        cubeWallsWithFeatures(fullWallFeatures(), offset = plainWallLampOffset())
    )

fun singleCellRoom() = BiomedBlockBuilder(
    block = Block(
        name = "cubeRoom",
        sides = sides(
            east = Sides.broadOpen,
            north = Sides.broadOpen,
            west = Sides.broadOpen,
            south = Sides.broadOpen
        ),
        attributes = setOf(CellAttribute.traversable),
        slots = squareOffsets(2)
    ),
    builder = singleCellRoomBuilder()
)
