package generation.architecture.building

import generation.architecture.*
import generation.architecture.definition.floorOffset
import generation.architecture.definition.impassable
import generation.misc.*
import generation.next.Builder
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
      position = input.position + floorOffset + align(config.meshes, alignWithCeiling)(mesh) + offset,
      orientation = orientation,
      texture = biomeTexture(biome, TextureGroup.floor)
  ))
}


fun floorMesh(mesh: MeshName, offset: Vector3 = Vector3.zero, orientation: Quaternion = Quaternion()) =
    blockBuilder(down = impassable, builder = floorMeshBuilder(mesh, offset, orientation))

fun halfFloorMesh(mesh: MeshName, offset: Vector3 = Vector3.zero, orientation: Quaternion = Quaternion()) =
    blockBuilder(builder = floorMeshBuilder(mesh, offset, orientation))

fun newSlopedFloorMesh(mesh: MeshName) = blockBuilder(down = impassable) { input ->
  val meshInfo = input.config.meshes[mesh]!!
  val slopeAngle = asin(cellLength / 3f / meshInfo.shape.x)
  val orientation = Quaternion()
      .rotateZ(applyTurns(input.turns))
      .rotateX(slopeAngle)
  floorMeshBuilder(mesh, offset = Vector3(0f, 0f, cellLength / 6f), orientation = orientation)(input)
}
