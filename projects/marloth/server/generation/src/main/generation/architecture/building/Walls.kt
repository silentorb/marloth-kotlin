package generation.architecture.building

import generation.architecture.cellHalfLength
import generation.architecture.definition.ConnectionType
import generation.architecture.newArchitectureMesh
import generation.elements.Direction
import generation.elements.horizontalDirections
import generation.misc.BiomeInfo
import generation.misc.GenerationConfig
import generation.misc.TextureGroup
import generation.misc.biomeTexture
import mythic.spatial.*
import scenery.MeshName
import scenery.enums.MeshId
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

fun directionRotation(direction: Direction): Float =
    when (direction) {
      Direction.east -> 0f
      Direction.north -> Pi * 0.5f
      Direction.west -> Pi
      Direction.south -> Pi * 1.5f
      else -> throw Error("Not supported")
    }

fun cubeWalls() = blockBuilder { input ->
  val cell = input.cell
  val grid = input.grid
  val connections = input.grid.connections
  val config = input.config
  val biome = input.biome
  val dice = input.dice

  val placeWall: (Map<ConnectionType, Set<MeshId>>) -> (Map.Entry<Direction, Vector3i>) -> Hand? = { meshMap ->
    { (direction, offset) ->
      val position = input.position + cellHalfLength + offset.toVector3() * cellHalfLength
      val angleZ = directionRotation(direction)
      val mesh = getSideMesh(dice, input.sides, direction, meshMap)
      if (mesh != null)
        newWallInternal(config, mesh.name, position, angleZ, biome)
      else
        null
    }
  }

  val (open, closed) =
      horizontalDirections.entries
          .filter { (direction, offset) ->
            val otherCell = cell + offset
            setOf(Direction.north, Direction.east).contains(direction) ||
                !grid.cells.containsKey(otherCell)
          }
          .partition { (_, offset) ->
            val otherCell = cell + offset
            containsConnection(connections, cell, otherCell)
          }

  listOf<Hand>()
      .plus(
          open
              .filter { dice.getInt(3) == 2 }
              .mapNotNull(placeWall(mapOf(
                  ConnectionType.doorway to setOf(MeshId.squareWallDoorway)
              ))))

      .plus(
          closed
              .mapNotNull(placeWall(mapOf(
                  ConnectionType.wall to setOf(MeshId.squareWall),
                  ConnectionType.window to setOf(MeshId.squareWallWindow)

              )))
      )
}
