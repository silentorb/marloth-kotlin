package generation.architecture

import generation.elements.Polyomino
import generation.next.Builder
import mythic.spatial.Vector3
import scenery.enums.MeshId

val floorOffset = Vector3(cellHalfLength, cellHalfLength, 0f)

fun newBuilders(): Map<Polyomino, Builder> =
    mapOf(
        PolyominoeDefinitions.singleCellRoom to { input ->
          listOf(newFloorMesh(input, MeshId.squareFloor.name))
              .plus(cubeWalls(input, MeshId.squareWall.name))
        }
    )
