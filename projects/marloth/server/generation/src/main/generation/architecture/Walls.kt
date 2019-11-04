package generation.architecture

import generation.elements.Direction
import generation.elements.horizontalDirections
import generation.misc.BiomeInfo
import generation.misc.GenerationConfig
import generation.misc.TextureGroup
import generation.misc.biomeTexture
import generation.next.BuilderInput
import mythic.spatial.*
import scenery.MeshName
import simulation.entities.ArchitectureElement
import simulation.main.Hand
import simulation.misc.Node
import simulation.misc.containsConnection

private fun newWallInternal(config: GenerationConfig, mesh: MeshName, position: Vector3, angleZ: Float, biome: BiomeInfo, node: Node? = null): Hand {
//  val biome = config.biomes[node.biome!!]!!
  val orientation = Quaternion().rotateZ(angleZ + quarterAngle)
  return newArchitectureMesh(
      architecture = ArchitectureElement(isWall = true),
      meshes = config.meshes,
      mesh = mesh,
      position = position,
      scale = Vector3.unit,
      orientation = orientation,
      node = node?.id ?: 0L,
      texture = biomeTexture(biome, TextureGroup.wall)
  )
}

//fun newWall(config: GenerationConfig, meshes: List<MeshName>, node: Node, position: Vector3, angleZ: Float): List<Hand> {
//  val meshInfo = config.meshes[meshes[0].toString()]!!
//  val upperOffset = Vector3(0f, 0f, meshInfo.shape.height)
//  return listOf(
//      newWallInternal(config, meshes.first(), position, angleZ, node),
//      newWallInternal(config, meshes.last(), position + upperOffset, angleZ, node)
//  )
//}

fun directionRotation(index: Int): Float =
    when (index) {
      Direction.east -> 0f
      Direction.north -> Pi * 0.5f
      Direction.west -> Pi
      Direction.south -> Pi * 1.5f
      else -> throw Error("Not supported")
    }

fun cubeWalls(input: BuilderInput, mesh: MeshName): List<Hand> {
  val cell = input.cell
  val connections = input.grid.connections
  val config = input.config
  val biome = input.biome
  return horizontalDirections
      .filter { direction -> !containsConnection(connections, cell, cell + direction.value) }
      .map { direction ->
        val position = input.position + cellHalfLength + direction.value.toVector3() * cellHalfLength
        val angleZ = directionRotation(direction.key)
        newWallInternal(config, mesh, position, angleZ, biome)
      }
}
