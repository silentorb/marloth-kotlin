package generation.architecture.building

import generation.architecture.definition.RandomMeshQuery
import generation.architecture.misc.BuilderInput
import generation.architecture.misc.GenerationConfig
import generation.architecture.old.cellHalfLength
import generation.architecture.old.newArchitectureMesh
import generation.general.*
import silentorb.mythic.spatial.*
import silentorb.mythic.scenery.MeshName
import simulation.entities.ArchitectureElement
import simulation.main.Hand

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
              meshQuery: RandomMeshQuery
): (Map.Entry<Direction, Vector3i>) -> Hand? = { (direction, offset) ->
  val position = input.position + cellHalfLength + offset.toVector3() * cellHalfLength + Vector3(0f, 0f, height)
  val angleZ = directionRotation(direction)
  val nothingChance = meshQuery.nothingChance
  val mesh = if (nothingChance == 0f || input.dice.getFloat() > nothingChance)
    input.selectMesh(meshQuery.query)
  else
    null

  if (mesh != null)
    newWallInternal(input.config, mesh, position, angleZ, input.biome)
  else
    null
}

fun selectMeshQuery(input: BuilderInput, direction: Direction): RandomMeshQuery? {
  val side = input.sides[direction]!!
  val connectionType = input.dice.takeOne(side)
  return input.connectionTypesToMeshQueries[connectionType]
}

fun cubeWalls(directions: Set<Direction> = horizontalDirections, height: Float = 0f) = blockBuilder { input ->
  val dice = input.dice
  val rotatedDirections = directions.map(rotateDirection(input.turns))
  val cell = input.cell
  val grid = input.grid

  horizontalDirectionVectors
      .filterKeys { rotatedDirections.contains(it) }
      .entries
      .filter { (direction, offset) ->
        val otherCell = cell + offset
        setOf(Direction.north, Direction.east).contains(direction) ||
            !grid.cells.containsKey(otherCell)
      }
      .mapNotNull { entry ->
        val query = selectMeshQuery(input, entry.key)
        if (query != null)
          placeWall(input, height, query)(entry)
        else
          null
      }
}
