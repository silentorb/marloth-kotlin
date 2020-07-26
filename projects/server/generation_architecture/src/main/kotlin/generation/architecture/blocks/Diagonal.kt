package generation.architecture.blocks

import generation.architecture.building.diagonalCorner
import generation.architecture.building.floorMesh
import generation.architecture.definition.Sides
import generation.architecture.definition.levelConnectors
import generation.architecture.definition.preferredHorizontalClosed
import generation.architecture.engine.BlockBuilder
import generation.architecture.engine.mergeBuilders
import generation.architecture.engine.sides
import generation.architecture.matrical.*
import generation.architecture.misc.squareOffsets
import generation.general.Block
import marloth.scenery.enums.MeshId
import silentorb.mythic.spatial.Vector3
import simulation.misc.CellAttribute

val diagnoseCorner: MatrixBlockBuilder = { input ->
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
