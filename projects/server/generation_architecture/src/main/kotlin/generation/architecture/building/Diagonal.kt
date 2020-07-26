package generation.architecture.building

import generation.architecture.definition.plainWallLampOffset
import generation.architecture.misc.Builder
import generation.general.Block
import generation.general.Direction
import generation.general.SideMap
import marloth.scenery.enums.MeshId
import silentorb.mythic.scenery.MeshName
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import simulation.misc.CellAttribute

fun randomDiagonalWall(mesh: MeshName, height: Float = 0f): Builder = { input ->
  val config = input.general.config
  val biome = input.biome
  val position = Vector3(0f, 0f, height)
  val scale = Vector3(1.0f, 1f, 1f)
  listOf(newWallInternal(config, mesh, position, Quaternion().rotateZ(Pi * 0.25f), biome, scale = scale))
}

//fun diagonalCornerFloor(name: String, upper: Level) = BlockBuilder(
//    block = Block(
//        name = name,
//        sides = sides(
//            up = upper.up,
//            east = upper.side,
//            north = upper.side,
//            west = Sides.preferredHorizontalClosed,
//            south = Sides.preferredHorizontalClosed
//        ),
//        attributes = setOf(CellAttribute.categoryDiagonal, CellAttribute.traversable)
//    ),
//    builder = mergeBuilders(
//        diagonalHalfFloorMesh(MeshId.squareFloorHalfDiagonal, upper.height),
//        randomDiagonalWall(MeshId.diagonalWall, upper.height)
//    )
//) //+
//+    withWallLamp(0.7f)(randomDiagonalWall(height))

fun diagonalCorner(name: String, height: Float, sides: SideMap, fallback: Builder): BlockBuilder =
    BlockBuilder(
        block = Block(
            name = name,
            sides = sides,
            attributes = setOf(CellAttribute.categoryDiagonal, CellAttribute.traversable)
        ),
        builder = { input ->
          if (input.neighbors.intersect(setOf(Direction.west, Direction.south)).any())
            fallback(input)
          else
            mergeBuilders(
                diagonalHalfFloorMesh(MeshId.squareFloorHalfDiagonal, height),
                randomDiagonalWall(MeshId.diagonalWall, height),
                cubeWallsWithFeatures(fullWallFeatures(), offset = plainWallLampOffset())
            )(input)
        }
    )
