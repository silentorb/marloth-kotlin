package generation.architecture

import generation.architecture.definition.BlockDefinitions
import generation.architecture.definition.PolyominoeDefinitions
import generation.elements.Block
import generation.elements.Polyomino
import generation.next.Builder
import mythic.spatial.Vector3
import mythic.spatial.Vector3i
import scenery.enums.MeshId

val floorOffset = Vector3(cellHalfLength, cellHalfLength, 0f)

fun newBuilders(): Map<Block, Builder> =
    mapOf(

        BlockDefinitions.singleCellRoom to { input ->
          listOf(newFloorMesh(input, MeshId.squareFloor.name))
              .plus(cubeWalls(input, MeshId.squareWall.name))
        },

//        PolyominoeDefinitions.spiralStairsSingle to { input ->
//          val upper = input.copy(
//              cell = input.cell + Vector3i(0, 0, 1),
//              position = input.position + Vector3(0f, 0f, cellLength)
//          )
//          listOf(newFloorMesh(input, MeshId.squareFloor.name))
//              .plus(cubeWalls(input, MeshId.squareWall.name))
//              .plus(newCurvedStaircases(input))
//              .plus(newFloorMesh(upper, MeshId.halfSquareFloor.name))
//              .plus(cubeWalls(upper, MeshId.squareWall.name))
//        },

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
        }
    )
