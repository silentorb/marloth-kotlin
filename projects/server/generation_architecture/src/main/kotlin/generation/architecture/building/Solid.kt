package generation.architecture.building

import generation.architecture.engine.Builder
import generation.architecture.matrical.mergeBuilders
import generation.general.Direction
import generation.general.horizontalDirections
import simulation.entities.Depiction

fun solidCubeBuilder(wall: Depiction, floor: Depiction, directions: Set<Direction> = horizontalDirections): Builder = { input ->
  throw Error("No longer supported")
//  directions.minus(sides).map(cubeWall(input, wall))
//      .plus(
//          listOfNotNull(
//              if (!sides.contains(Direction.down)) floorMesh(floor)(input) else null
//          ).flatten()
//      )
}

fun solidDiagonalBuilder(wall: Depiction, floor: Depiction): Builder = mergeBuilders(
    diagonalHalfFloorMesh(floor),
    diagonalWall(wall),
    solidCubeBuilder(wall, floor, setOf(Direction.east, Direction.north))
//    cubeWallsWithFeatures(fullWallFeatures(), lampOffset = plainWallLampOffset())
)
