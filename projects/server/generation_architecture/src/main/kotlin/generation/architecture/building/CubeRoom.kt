package generation.architecture.building

import generation.architecture.engine.Builder
import generation.architecture.matrical.mergeBuilders
import silentorb.mythic.spatial.Vector3
import simulation.entities.Depiction

fun plainWallLampOffset() = Vector3(0f, 0f, -1f)

fun cubeRoomBuilder(floor: Depiction, wall: Depiction) = mergeBuilders(
    floorMesh(floor),
    roomWalls(wall)
)

fun singleCellRoomBuilder(floor: Depiction, wall: Depiction): Builder =
    mergeBuilders(
        floorMesh(floor),
        cubeWallsWithFeatures(
            features = fullWallFeatures(),
            wallDepiction = wall,
            lampOffset = plainWallLampOffset()
        )
    )
