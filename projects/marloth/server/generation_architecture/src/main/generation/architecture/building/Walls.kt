package generation.architecture.building

import generation.architecture.old.cellHalfLength
import generation.architecture.definition.ConnectionType
import generation.architecture.old.newArchitectureMesh
import generation.general.*
import generation.architecture.misc.BiomeInfo
import generation.architecture.misc.GenerationConfig
import generation.architecture.misc.TextureGroup
import generation.architecture.misc.biomeTexture
import generation.architecture.misc.BuilderInput
import mythic.spatial.*
import scenery.MeshName
import scenery.enums.MeshId
import simulation.entities.ArchitectureElement
import simulation.main.Hand
import simulation.misc.containsConnection

val openWallPool = mapOf(
    ConnectionType.doorway to setOf(MeshId.squareWallDoorway)
)

val closedWallPool = mapOf(
    ConnectionType.wall to setOf(MeshId.squareWall, MeshId.squareWallHalfHeight, MeshId.squareWallQuarterHeight),
    ConnectionType.window to setOf(MeshId.squareWallWindow)
)

fun newWallInternal(config: GenerationConfig, mesh: MeshName, position: Vector3, angleZ: Float, biome: BiomeInfo,
                    scale: Vector3 = Vector3.unit): Hand {
  val orientation = Quaternion().rotateZ(angleZ + quarterAngle)
  return newArchitectureMesh(
      architecture = ArchitectureElement(isWall = true),
      meshes = config.meshes,
      mesh = mesh,
      position = position,
      scale = scale,
      orientation = orientation,
      texture = biomeTexture(biome, TextureGroup.wall)
  )
}

fun directionRotation(direction: Direction): Float =
    when (direction) {
      Direction.east -> 0f
      Direction.north -> Pi * 0.5f
      Direction.west -> Pi
      Direction.south -> Pi * 1.5f
      else -> throw Error("Not supported")
    }

fun placeWall(input: BuilderInput, height: Float,
              hasUpperStory: Boolean,
              meshMap: Map<ConnectionType, Set<MeshId>>
): (Map.Entry<Direction, Vector3i>) -> Hand? = { (direction, offset) ->
  val position = input.position + cellHalfLength + offset.toVector3() * cellHalfLength + Vector3(0f, 0f, height)
  val angleZ = directionRotation(direction)
  val mesh = getSideMesh(input.dice, input.sides, direction, meshMap)
  if (mesh != null)
    newWallInternal(input.config, mesh.name, position, angleZ, input.biome)
  else
    null
}

fun hasUpperStory(blockGrid: BlockGrid, cell: Vector3i): Boolean {
  val upperBlock = blockGrid[cell + Vector3i(0, 0, 1)]
  return upperBlock != null && upperBlock.sides.getValue(Direction.down).contains(ConnectionType.wall)
}

fun cubeWalls(
    directions: Set<Direction> = horizontalDirections,
    height: Float = 0f) = blockBuilder { input ->
  val cell = input.cell
  val grid = input.grid
  val connections = input.grid.connections
  val config = input.config
  val biome = input.biome
  val dice = input.dice
  val rotatedDirections = directions.map(rotateDirection(input.turns))
  val hasUpperStory = hasUpperStory(input.blockGrid, cell)
  val (open, closed) =
      horizontalDirectionVectors
          .filterKeys { rotatedDirections.contains(it) }
          .entries
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
              .mapNotNull(placeWall(input, height, hasUpperStory, openWallPool)))

      .plus(
          closed
              .mapNotNull(placeWall(input, height, hasUpperStory, closedWallPool))
      )
}
