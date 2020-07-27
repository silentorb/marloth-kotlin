package generation.architecture.blocks

import generation.architecture.building.*
import generation.architecture.definition.BiomeId
import generation.architecture.definition.Sides
import generation.architecture.definition.uniqueConnection
import generation.architecture.matrical.*
import generation.general.Block
import generation.general.Direction
import marloth.scenery.enums.MeshId
import silentorb.mythic.spatial.Vector3
import simulation.misc.CellAttribute
import simulation.misc.cellLength

val homeSides = uniqueConnection("homeSides")

val homeBlock1 = applyBiomedBlockBuilder(BiomeId.home)(
    BiomedBlockBuilder(
        block = Block(
            name = "home1",
            sides = sides(
                east = Sides.doorway,
                north = homeSides.first
            ),
            attributes = setOf(CellAttribute.home, CellAttribute.traversable)
        ),
        builder = mergeBuilders(
            floorMesh(MeshId.squareFloor),
            floorMesh(MeshId.squareFloor, offset = Vector3(0f, 0f, cellLength - 1f)),
            placeCubeRoomWalls(MeshId.squareWallWindow, setOf(Direction.west)),
            placeCubeRoomWalls(MeshId.squareWallDoorway, setOf(Direction.east)),
            placeCubeRoomWalls(MeshId.squareWall, setOf(Direction.south)),
            handBuilder(cubeWallLamp(Direction.south, plainWallLampOffset()))
        )
    )
)

fun homeBlock2() = applyBiomedBlockBuilder(BiomeId.home)(
    BiomedBlockBuilder(
        block = Block(
            name = "home2",
            sides = sides(
                south = homeSides.second
            ),
            attributes = setOf(CellAttribute.home, CellAttribute.traversable)
        ),
        builder = mergeBuilders(
            floorMesh(MeshId.squareFloor),
            floorMesh(MeshId.squareFloor, offset = Vector3(0f, 0f, cellLength - 1f)),
            placeCubeRoomWalls(MeshId.squareWallWindow, setOf(Direction.north)),
            placeCubeRoomWalls(MeshId.squareWallDoorway, setOf(Direction.south)),
            placeCubeRoomWalls(MeshId.squareWall, setOf(Direction.east, Direction.west)),
            handBuilder(cubeWallLamp(Direction.west, plainWallLampOffset()))
        )
    )
)
