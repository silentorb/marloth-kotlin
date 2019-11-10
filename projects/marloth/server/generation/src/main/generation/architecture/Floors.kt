package generation.architecture

import generation.misc.*
import generation.next.BuilderInput
import mythic.spatial.Vector3i
import scenery.MeshName
import simulation.entities.ArchitectureElement
import simulation.main.Hand

fun newFloorMesh(input: BuilderInput, mesh: MeshName): Hand {
  val config = input.config
  val biome = input.biome
  return newArchitectureMesh(
      architecture = ArchitectureElement(isWall = false),
      meshes = config.meshes,
      mesh = mesh,
      position = input.position + floorOffset + align(config.meshes, alignWithCeiling)(mesh),
      texture = biomeTexture(biome, TextureGroup.floor)
  )
}
