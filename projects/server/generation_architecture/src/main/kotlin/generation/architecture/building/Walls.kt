package generation.architecture.building

import generation.architecture.misc.ArchitectureInput
import generation.architecture.misc.Builder
import generation.architecture.misc.BuilderInput
import generation.architecture.misc.GenerationConfig
import generation.architecture.old.newArchitectureMesh
import generation.general.*
import marloth.scenery.enums.MeshId
import silentorb.mythic.scenery.MeshName
import silentorb.mythic.spatial.*
import simulation.main.Hand
import simulation.misc.*

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
      Direction.north -> quarterAngle
      Direction.west -> Pi
      Direction.south -> Pi * 1.5f
      else -> throw Error("Not supported")
    }

fun cylinderWalls() = blockBuilder { input ->
  val diagonals = (0 until 4)
      .map { Pair(MeshId.arcWall8thA, (0.25f + it * 0.5f) * Pi) }

  val optionalWalls = horizontalDirectionVectors
//      .filterKeys { it == Direction.south }
//      .filterValues { offset ->
//        !containsConnection(input.general.grid.connections, input.cell, input.cell + offset)
//      }
      .map { (direction, _) ->
        Pair(MeshId.arcWall8thB, directionRotation(direction))
      }

  diagonals.plus(optionalWalls)
//      optionalWalls
      .map { (mesh, angleZ) ->
        val facingOffset = Quaternion().rotateZ(angleZ) * Vector3(cellHalfLength - 0.25f, 0f, 0f)
        WallPlacement(
            config = input.general.config,
            mesh = mesh,
            position = cellCenterOffset + facingOffset,
            angleZ = angleZ + Pi,
            biome = input.biome
        )
      }
      .map(::newWallInternal)
}

fun getTruncatedWallMesh(input: BuilderInput, height: Float): MeshName {
  val scale = height / cellLength
  return when {
//    !input.sides[Direction.up]!!.contains(ConnectionType.extraHeadroom) -> MeshId.squareWall
    scale <= 0.5f -> MeshId.squareWallHalfHeight // Don't currently have a 3/4 height wall mesh
    else -> MeshId.squareWallQuarterHeight
  }
}

fun placeWall(general: ArchitectureInput, mesh: MeshName, position: Vector3, direction: Direction, biome: BiomeName): Hand {
  val angleZ = directionRotation(direction)
  val biomeInfo = general.config.biomes[biome]!!
  return newWallInternal(WallPlacement(general.config, mesh, position, angleZ, biomeInfo))
}

fun placeCubeRoomWalls(mesh: MeshName): Builder = { input ->
  val general = input.general
  val biome = input.biome
  val neighbors = input.neighbors
  horizontalDirectionVectors
      .filterKeys { !neighbors.contains(it) }
      .map { (direction, offset) ->
        placeWall(general, mesh, offset.toVector3() * cellHalfLength, direction, biome.name)
      }
}
