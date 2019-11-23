package generation.architecture.building

import generation.architecture.misc.BuilderInput
import generation.architecture.misc.GenerationConfig
import generation.architecture.misc.MeshQuery
import generation.architecture.old.cellHalfLength
import generation.architecture.old.newArchitectureMesh
import generation.general.*
import mythic.spatial.*
import scenery.MeshName
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
              meshQuery: MeshQuery
): (Map.Entry<Direction, Vector3i>) -> Hand? = { (direction, offset) ->
  val position = input.position + cellHalfLength + offset.toVector3() * cellHalfLength + Vector3(0f, 0f, height)
  val angleZ = directionRotation(direction)
//  val mesh = getSideMesh(input.dice, input.sides, direction, meshMap)
  val mesh = input.selectMesh(meshQuery)
  println(mesh)
  if (mesh != null)
    newWallInternal(input.config, mesh, position, angleZ, input.biome)
  else
    null
}

fun selectMeshQuery(input: BuilderInput, direction: Direction): MeshQuery? {
  val side = input.sides[direction]!!
  val connectionType = input.dice.takeOne(side)
  return input.connectionTypesToMeshQueries[connectionType]
}

fun cubeWalls(directions: Set<Direction> = horizontalDirections, height: Float = 0f) = blockBuilder { input ->
  val dice = input.dice
  val rotatedDirections = directions.map(rotateDirection(input.turns))

  horizontalDirectionVectors
      .filterKeys { rotatedDirections.contains(it) }
      .entries
      .mapNotNull { entry ->
        val query = selectMeshQuery(input, entry.key)
        if (query != null)
          placeWall(input, height, query)(entry)
        else
          null
      }
}
