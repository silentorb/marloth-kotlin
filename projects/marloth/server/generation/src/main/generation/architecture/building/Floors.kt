package generation.architecture.building

import generation.architecture.*
import generation.architecture.definition.floorOffset
import generation.misc.*
import generation.next.Builder
import generation.next.BuilderInput
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import scenery.MeshName
import simulation.entities.ArchitectureElement
import simulation.main.Hand
import kotlin.math.asin

fun floorMesh(mesh: MeshName, offset: Vector3 = Vector3.zero,
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

fun newSlopedFloorMesh(mesh: MeshName): Builder = { input ->
  val meshInfo = input.config.meshes[mesh]!!
  val slopeAngle = asin(cellLength / 3f / meshInfo.shape.x)
  val orientation = Quaternion()
      .rotateZ(applyTurns(input.turns))
      .rotateX(slopeAngle)
  floorMesh(mesh, offset = Vector3(0f, 0f, cellLength / 6f), orientation = orientation)(input)
}
