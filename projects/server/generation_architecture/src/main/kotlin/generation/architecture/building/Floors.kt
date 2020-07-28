package generation.architecture.building

import generation.architecture.engine.*
import generation.architecture.matrical.BiomedBuilder
import generation.general.TextureGroup
import generation.general.biomeTexture
import silentorb.mythic.scenery.MeshName
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.quarterAngle
import simulation.misc.cellHalfLength
import simulation.misc.cellLength
import kotlin.math.asin

fun floorMeshBuilder(mesh: MeshName, offset: Vector3 = Vector3.zero,
                     orientation: Quaternion = Quaternion()): BiomedBuilder = { input ->
  val config = input.general.config
  val biome = input.biome
  listOf(newArchitectureMesh(
      meshes = config.meshes,
      mesh = mesh,
      position = Vector3(0f, 0f, -cellHalfLength) + align(config.meshes, alignWithCeiling)(mesh) + offset + Vector3(0f, 0f, 0.01f),
      orientation = orientation,
      texture = biomeTexture(biome, TextureGroup.floor)
  ))
}

fun floorMesh(mesh: MeshName, offset: Vector3 = Vector3.zero, orientation: Quaternion = Quaternion()) =
    floorMeshBuilder(mesh, offset, orientation)

fun diagonalHalfFloorMesh(mesh: MeshName, height: Float): BiomedBuilder = { input ->
  val position = Vector3(0f, 0f, height)
  val orientation = Quaternion().rotateZ(-quarterAngle)
  floorMeshBuilder(mesh, offset = position, orientation = orientation)(input)
}

fun newSlopedFloorMesh(mesh: MeshName, height: Float): BiomedBuilder = { input ->
  val meshInfo = input.general.config.meshes[mesh]!!
  val slopeAngle = asin(cellLength / 4f / meshInfo.shape!!.x) - 0.007f
  val orientation = Quaternion()
      .rotateY(-slopeAngle)
      .rotateZ(quarterAngle)
  floorMeshBuilder(mesh, offset = Vector3(0f, 0f, height + cellLength / 8f), orientation = orientation)(input)
}

fun newSlopeEdgeBlock(mesh: MeshName, height: Float, ledgeTurns: Int): BiomedBuilder = { input ->
  val offset = Quaternion().rotateZ(applyTurnsOld(ledgeTurns))
      .transform(Vector3(0f, cellLength / 4f, 0f))
  val position = offset + Vector3(0f, 0f, height)
  floorMeshBuilder(mesh, offset = position)(input)
}
