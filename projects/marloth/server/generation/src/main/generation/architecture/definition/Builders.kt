package generation.architecture.definition

import generation.architecture.*
import generation.architecture.building.*
import generation.elements.Block
import generation.next.Builder
import mythic.spatial.Vector3
import scenery.enums.MeshId

val floorOffset = Vector3(cellHalfLength, cellHalfLength, 0f)
/*
fun newBuilders(): Map<Block, Builder> =
    mapOf(

        BlockDefinitions.singleCellRoom to compose(
            floorMesh(MeshId.squareFloor.name),
            cubeWalls()
        ),

        BlockDefinitions.stairBottom to compose(
            floorMesh(MeshId.squareFloor.name),
            cubeWalls(),
            curvedStaircases
        ),

        BlockDefinitions.stairMiddle to compose(
            cubeWalls(),
            curvedStaircases
        ),

        BlockDefinitions.stairTop to compose(
            cubeWalls(),
            floorMesh(MeshId.halfSquareFloor.name)
        ),

        BlockDefinitions.halfStepRoom to compose(
            newHalfStepFloorMesh(MeshId.squareFloor.name),
            cubeWalls()
        ),

        BlockDefinitions.lowerHalfStepSlope to compose(
            floorMesh(MeshId.squareFloor.name),
            newSlopedFloorMesh(MeshId.squareFloor.name),
            cubeWalls()
        )

    )
*/
