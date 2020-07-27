package generation.architecture.building

import generation.architecture.blocks.plainWallLampOffset
import generation.architecture.matrical.BlockBuilder
import generation.architecture.matrical.mergeBuilders
import generation.architecture.matrical.BiomedBuilder
import generation.general.Block
import generation.general.Direction
import generation.general.SideMap
import marloth.scenery.enums.MeshId
import silentorb.mythic.scenery.MeshName
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import simulation.misc.CellAttribute

fun randomDiagonalWall(mesh: MeshName, height: Float = 0f): BiomedBuilder = { input ->
  val config = input.general.config
  val biome = input.biome
  val position = Vector3(0f, 0f, height)
  val scale = Vector3(1.0f, 1f, 1f)
  listOf(newWallInternal(config, mesh, position, Quaternion().rotateZ(Pi * 0.25f), biome, scale = scale))
}

fun diagonalCorner(height: Float, fallback: BiomedBuilder): BiomedBuilder = { input ->
  if (input.neighbors.intersect(setOf(Direction.west, Direction.south)).any())
    fallback(input)
  else
    mergeBuilders(
        diagonalHalfFloorMesh(MeshId.squareFloorHalfDiagonal, height),
        randomDiagonalWall(MeshId.diagonalWall, height),
        cubeWallsWithFeatures(fullWallFeatures(), offset = plainWallLampOffset())
    )(input)
}
