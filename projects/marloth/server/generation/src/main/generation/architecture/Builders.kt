package generation.architecture

import generation.architecture.definition.BlockDefinitions
import generation.elements.Block
import generation.next.Builder
import mythic.spatial.Vector3
import scenery.enums.MeshId

val floorOffset = Vector3(cellHalfLength, cellHalfLength, 0f)

fun newBuilders(): Map<Block, Builder> =
    mapOf(

        BlockDefinitions.singleCellRoom to { input ->
          listOf(newFloorMesh(input, MeshId.squareFloor.name))
              .plus(cubeWalls(input, MeshId.squareWall.name))
        },

        BlockDefinitions.stairBottom to { input ->
          listOf(newFloorMesh(input, MeshId.squareFloor.name))
              .plus(cubeWalls(input, MeshId.squareWall.name))
              .plus(newCurvedStaircases(input))
        },

        BlockDefinitions.stairMiddle to { input ->
          cubeWalls(input, MeshId.squareWall.name)
              .plus(newCurvedStaircases(input))
        },

        BlockDefinitions.stairTop to { input ->
          cubeWalls(input, MeshId.squareWall.name)
              .plus(newFloorMesh(input, MeshId.halfSquareFloor.name))
        },

        BlockDefinitions.halfStepRoom to { input ->
          listOf(newFloorMesh(input, MeshId.squareFloor.name, Vector3(0f, 0f, cellLength / 3f)))
              .plus(cubeWalls(input, MeshId.squareWall.name))
        },

        BlockDefinitions.lowerHalfStepSlope to { input ->
          listOf(newFloorMesh(input, MeshId.squareFloor.name))
              .plus(newSlopedFloorMesh(input, MeshId.squareFloor.name))
              .plus(cubeWalls(input, MeshId.squareWall.name))
        }

    )
