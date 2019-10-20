package generation.architecture

import generation.misc.GenerationConfig
import generation.misc.TextureGroup
import generation.misc.biomeTexture
import mythic.spatial.Pi
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import mythic.spatial.quarterAngle
import randomly.Dice
import scenery.MeshName
import simulation.entities.ArchitectureElement
import simulation.main.Hand
import simulation.misc.Node

fun newWall(config: GenerationConfig, mesh: MeshName, dice: Dice, node: Node, position: Vector3, angleZ: Float): Hand {
  val biome = config.biomes[node.biome!!]!!
  val randomHorizontalFlip = getHorizontalFlip(dice, config.meshes[mesh.toString()]!!)
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
