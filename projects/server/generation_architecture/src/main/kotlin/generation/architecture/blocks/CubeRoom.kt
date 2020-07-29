package generation.architecture.blocks

import generation.architecture.building.cubeWallsWithFeatures
import generation.architecture.building.floorMesh
import generation.architecture.building.fullWallFeatures
import generation.architecture.building.tieredWalls
import generation.architecture.definition.levelSides
import generation.architecture.engine.squareOffsets
import generation.architecture.matrical.*
import generation.general.Block
import marloth.scenery.enums.MeshId
import silentorb.mythic.spatial.Vector3
import simulation.misc.CellAttribute

fun tieredSquareFloorBuilder(level: Int) =
    floorMesh(MeshId.squareFloor, Vector3(0f, 0f, getLevelHeight(level)))

fun tieredCubeBuilder(level: Int) = mergeBuilders(
    tieredSquareFloorBuilder(level),
    tieredWalls(level)
)

val squareRoom: MatrixBlockBuilder = { input ->
  val level = input.level
  val open = levelSides[level].open
  listOf(
      BiomedBlockBuilder(
          block = Block(
              name = "halfStepRoom$level",
              sides = sides(
                  east = open,
                  north = open,
                  west = open,
                  south = open
              ),
              attributes = setOf(CellAttribute.traversable),
              slots = squareOffsets(2).map { it + Vector3(0f, 0f, getLevelHeight(level)) }
          ),
          builder = tieredCubeBuilder(level)
      )
  )
}

fun singleCellRoomBuilder(level: Int): BiomedBuilder =
    mergeBuilders(
        tieredSquareFloorBuilder(level),
        cubeWallsWithFeatures(
            features = fullWallFeatures(),
            offset = Vector3(0f, 0f, getLevelHeight(level)),
            lampOffset = plainWallLampOffset())
    )
