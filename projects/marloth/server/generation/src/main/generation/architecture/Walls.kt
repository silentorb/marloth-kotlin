package generation.architecture

import generation.misc.GenerationConfig
import generation.misc.TextureGroup
import generation.misc.biomeTexture
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import mythic.spatial.quarterAngle
import randomly.Dice
import scenery.MeshName
import simulation.entities.ArchitectureElement
import simulation.main.Hand
import simulation.misc.Node

private fun newWallInternal(config: GenerationConfig, mesh: MeshName, node: Node, position: Vector3, angleZ: Float): Hand {
  val biome = config.biomes[node.biome!!]!!
  val orientation = Quaternion().rotateZ(angleZ + quarterAngle)
  return newArchitectureMesh(
      architecture = ArchitectureElement(isWall = true),
      meshes = config.meshes,
      mesh = mesh,
      position = position + floorOffset + align(config.meshes, alignWithFloor)(mesh),
      scale = Vector3.unit,
      orientation = orientation,
      node = node.id,
      texture = biomeTexture(biome, TextureGroup.wall)
  )
}

fun newWall(config: GenerationConfig, meshes: List<MeshName>, node: Node, position: Vector3, angleZ: Float): List<Hand> {
  val meshInfo = config.meshes[meshes[0].toString()]!!
  val upperOffset = Vector3(0f, 0f, meshInfo.shape.height)
  return listOf(
      newWallInternal(config, meshes.first(), node, position, angleZ),
      newWallInternal(config, meshes.last(), node, position + upperOffset, angleZ)
  )
}
