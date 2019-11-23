package generation.architecture.building

import generation.architecture.definition.impassableHorizontal
import generation.architecture.old.*
import generation.architecture.misc.Builder
import generation.general.TextureGroup
import generation.general.biomeTexture
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import scenery.MeshName
import simulation.entities.ArchitectureElement
import kotlin.math.asin

fun floorMeshBuilder(mesh: MeshName, offset: Vector3 = Vector3.zero,
                     orientation: Quaternion = Quaternion()): Builder = { input ->
  val config = input.config
  val biome = input.biome
  listOf(newArchitectureMesh(
      architecture = ArchitectureElement(isWall = false),
      meshes = config.meshes,
      mesh = mesh,
      position = input.position + floorOffset + align(config.meshes, alignWithCeiling)(mesh) + offset + Vector3(0f, 0f, 0.01f),
      orientation = orientation,
      texture = biomeTexture(biome, TextureGroup.floor)
  ))
}

fun floorMesh(mesh: MeshName, offset: Vector3 = Vector3.zero, orientation: Quaternion = Quaternion()) =
    blockBuilder(down = impassableHorizontal, builder = floorMeshBuilder(mesh, offset, orientation))

fun halfFloorMesh(mesh: MeshName, offset: Vector3 = Vector3.zero, orientation: Quaternion = Quaternion()) =
    blockBuilder(builder = floorMeshBuilder(mesh, offset, orientation))

fun diagonalHalfFloorMesh(mesh: MeshName, height: Float) =
    blockBuilder { input ->
      val angle = applyTurns(input.turns)
//      val position = Vector3(-5f, 5f, 0f).transform(Matrix().rotateZ(angle))
//      val position = Vector3(-5f, -5f, -0.01f)
      val position = Vector3(0f, 0f, height)
      val orientation = Quaternion()
          .rotateZ(angle)
      floorMeshBuilder(mesh, offset = position, orientation = orientation)(input)
    }

fun newSlopedFloorMesh(mesh: MeshName, height: Float) = blockBuilder(down = impassableHorizontal) { input ->
  val meshInfo = input.config.meshes[mesh]!!
  val slopeAngle = asin(cellLength / 4f / meshInfo.shape!!.x)
  val orientation = Quaternion()
      .rotateZ(applyTurns(input.turns))
      .rotateX(slopeAngle)
  floorMeshBuilder(mesh, offset = Vector3(0f, 0f, height + cellLength / 8f), orientation = orientation)(input)
}
