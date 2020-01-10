package generation.architecture.building

import generation.architecture.definition.RandomMeshQuery
import generation.architecture.misc.BuilderInput
import generation.architecture.misc.GenerationConfig
import simulation.misc.cellHalfLength
import generation.architecture.old.newArchitectureMesh
import generation.general.*
import silentorb.mythic.spatial.*
import silentorb.mythic.scenery.MeshName
import simulation.main.Hand
import simulation.misc.CellMap

data class WallPlacement(
    val config: GenerationConfig,
    val mesh: MeshName,
    val position: Vector3,
    val angleZ: Float,
    val biome: BiomeInfo
)

fun newWallInternal(config: GenerationConfig, mesh: MeshName, position: Vector3, angleZ: Float, biome: BiomeInfo,
                    scale: Vector3 = Vector3.unit): Hand {
  val orientation = Quaternion().rotateZ(angleZ + quarterAngle)
  return newArchitectureMesh(
      meshes = config.meshes,
      mesh = mesh,
      position = position,
      orientation = orientation,
      texture = biomeTexture(biome, TextureGroup.wall),
      scale = scale
  )
}

fun newWallInternal(placement: WallPlacement) =
    newWallInternal(placement.config, placement.mesh, placement.position, placement.angleZ, placement.biome)

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
): (Direction, Vector3i) -> WallPlacement? = { direction, offset ->
  val position = input.position + cellHalfLength + offset.toVector3() * cellHalfLength + Vector3(0f, 0f, height)
  val angleZ = directionRotation(direction)
  val nothingChance = meshQuery.nothingChance
  val mesh = if (nothingChance == 0f || input.general.dice.getFloat() > nothingChance)
    input.general.selectMesh(meshQuery.query)
  else
    null

  if (mesh != null)
    WallPlacement(input.general.config, mesh, position, angleZ, input.biome)
//    newWallInternal(input.config, mesh, position, angleZ, input.biome)
  else
    null
}

fun selectMeshQuery(input: BuilderInput, direction: Direction): RandomMeshQuery? {
  val side = input.sides[direction]!!
  val connectionType = input.general.dice.takeOne(side)
  return input.general.connectionTypesToMeshQueries[connectionType]
}

fun getCubeWallDirections(directions: Set<Direction>, cells: CellMap,
                          cell: Vector3i, turns: Int): List<Map.Entry<Direction, Vector3i>> {
  val rotatedDirections = directions.map(rotateDirection(turns))
  return horizontalDirectionVectors
      .filterKeys { rotatedDirections.contains(it) }
      .entries
      .filter { (direction, offset) ->
        val otherCell = cell + offset
        setOf(Direction.north, Direction.east).contains(direction) ||
            !cells.containsKey(otherCell)
      }
}

fun cubeWallsPlacement(directions: Set<Direction> = horizontalDirections, height: Float = 0f) = { input: BuilderInput ->
  val cell = input.cell
  val grid = input.general.grid
  val processedDirections = getCubeWallDirections(directions, grid.cells, cell, input.turns)

  processedDirections
      .mapNotNull { (direction, offset) ->
        val query = selectMeshQuery(input, direction)
        if (query != null)
          placeWall(input, height, query)(direction, offset)
        else
          null
      }
}

fun cubeWallsWithLamps(directions: Set<Direction> = horizontalDirections,
                       height: Float = 0f, lampRate: Float) = blockBuilder { input ->
  listOf()
//  val wallPlacements = cubeWallsPlacement(directions, height)(input)
//  val hasLamp = lampRate == 1f || input.general.dice.getFloat() <= lampRate
//  val walls = wallPlacements.map(::newWallInternal)
//  if (hasLamp)
//    walls.plus(
//        input.general.dice.take(wallPlacements, 1).map(addWallLamp(input))
//    )
//  else
//    walls
}

fun cubeWalls(directions: Set<Direction> = horizontalDirections, height: Float = 0f) = blockBuilder { input ->
  listOf()
//  cubeWallsPlacement(directions, height)(input)
//      .map(::newWallInternal)
}
