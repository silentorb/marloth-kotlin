package generation.architecture.building

import generation.architecture.engine.Builder
import generation.architecture.matrical.mergeBuilders
import generation.general.Direction
import generation.general.horizontalDirections
import marloth.scenery.enums.MeshId
import simulation.entities.Depiction

fun solidCubeBuilder(wall: Depiction, floor: Depiction): Builder = { input ->
  val sides = input.neighbors
  horizontalDirections.minus(sides).map(cubeWall(input, wall))
      .plus(
          listOfNotNull(
              if (!sides.contains(Direction.down)) floorMesh(floor)(input) else null
          ).flatten()
      )
}

fun solidDiagonalBuilder(wall: Depiction, floor: Depiction): Builder = mergeBuilders(
    diagonalHalfFloorMesh(floor),
    diagonalWall(wall),
    solidCubeBuilder(wall, floor)
//    cubeWallsWithFeatures(fullWallFeatures(), lampOffset = plainWallLampOffset())
)
