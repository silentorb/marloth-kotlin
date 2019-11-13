package generation.architecture.definition

import generation.architecture.*
import generation.architecture.building.*
import generation.elements.Block
import generation.next.Builder
import mythic.spatial.Vector3
import scenery.enums.MeshId
import simulation.main.Hand

val floorOffset = Vector3(cellHalfLength, cellHalfLength, 0f)

fun newBuilders(): Map<Block, Builder> =
    mapOf(

        BlockDefinitions.singleCellRoom to compose(
            floorMesh(MeshId.squareFloor.name),
            cubeWalls(MeshId.squareWall.name)
        ),

        BlockDefinitions.stairBottom to compose(
            floorMesh(MeshId.squareFloor.name),
            cubeWalls(MeshId.squareWall.name),
            curvedStaircases
        ),

        BlockDefinitions.stairMiddle to compose(
            cubeWalls(MeshId.squareWall.name),
            curvedStaircases
        ),

        BlockDefinitions.stairTop to compose(
            cubeWalls(MeshId.squareWall.name),
            floorMesh(MeshId.halfSquareFloor.name)
        ),

        BlockDefinitions.halfStepRoom to compose(
            newHalfStepFloorMesh(MeshId.squareFloor.name),
            cubeWalls(MeshId.squareWall.name)
        ),

        BlockDefinitions.lowerHalfStepSlope to compose(
            floorMesh(MeshId.squareFloor.name),
            newSlopedFloorMesh(MeshId.squareFloor.name),
            cubeWalls(MeshId.squareWall.name)
        )

    )
