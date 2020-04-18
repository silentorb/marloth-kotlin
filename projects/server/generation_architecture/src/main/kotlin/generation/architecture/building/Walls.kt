package generation.architecture.building

import generation.architecture.misc.GenerationConfig
import generation.architecture.old.newArchitectureMesh
import generation.general.*
import marloth.definition.enums.MeshId
import silentorb.mythic.spatial.times
import silentorb.mythic.scenery.MeshName
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.quarterAngle
import simulation.main.Hand
import simulation.misc.cellCenterOffset
import simulation.misc.cellHalfLength
import simulation.misc.containsConnection

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

fun cylinderWalls() = blockBuilder { input ->
  val diagonals = (0 until 4)
      .map { Pair(MeshId.arcWall8thA.name, (0.25f + it * 0.5f) * Pi) }

  val optionalWalls = horizontalDirectionVectors
//      .filterKeys { it == Direction.south }
      .filterValues { offset ->
        !containsConnection(input.general.grid.connections, input.cell, input.cell + offset)
      }
      .map { (direction, _) ->
        Pair(MeshId.arcWall8thB.name, directionRotation(direction))
      }

  diagonals.plus(optionalWalls)
//      optionalWalls
      .map { (mesh, angleZ) ->
        val facingOffset = Quaternion().rotateZ(angleZ) * Vector3(cellHalfLength - 0.25f, 0f, 0f)
        WallPlacement(
            config = input.general.config,
            mesh = mesh,
            position = input.position + cellCenterOffset + facingOffset,
            angleZ = angleZ + Pi,
            biome = input.biome
        )
      }
      .map(::newWallInternal)
}
