package generation.architecture.building

import generation.architecture.engine.ArchitectureInput
import generation.architecture.engine.Builder
import generation.architecture.engine.GenerationConfig
import generation.architecture.engine.newArchitectureMesh
import generation.architecture.matrical.BiomedBuilder
import generation.architecture.matrical.BiomedBuilderInput
import generation.general.*
import marloth.scenery.enums.MeshId
import silentorb.mythic.scenery.MeshName
import silentorb.mythic.spatial.*
import simulation.main.Hand
import simulation.misc.*
import kotlin.math.min

data class WallPlacement(
    val config: GenerationConfig,
    val mesh: MeshName,
    val position: Vector3,
    val orientation: Quaternion,
    val biome: BiomeInfo
)

fun getWallOrientation(direction: Direction): Quaternion {
  val angleZ = directionRotation(direction)
  return Quaternion().rotateZ(angleZ)
}

fun newWallInternal(config: GenerationConfig, mesh: MeshName, position: Vector3, orientation: Quaternion, biome: BiomeInfo,
                    scale: Vector3 = Vector3.unit): Hand {
  return newArchitectureMesh(
      meshes = config.meshes,
      mesh = mesh,
      position = position,
      orientation = orientation.rotateZ(quarterAngle),
      texture = biomeTexture(biome, TextureGroup.wall),
      scale = scale
  )
}

fun newWallInternal(placement: WallPlacement) =
    newWallInternal(placement.config, placement.mesh, placement.position, placement.orientation, placement.biome)

fun directionRotation(direction: Direction): Float =
    when (direction) {
      Direction.east -> 0f
      Direction.north -> quarterAngle
      Direction.west -> Pi
      Direction.south -> Pi * 1.5f
      else -> throw Error("Not supported")
    }

//fun cylinderWalls() = blockBuilder { input ->
//  val diagonals = (0 until 4)
//      .map { Pair(MeshId.arcWall8thA, (0.25f + it * 0.5f) * Pi) }
//
//  val optionalWalls = horizontalDirectionVectors
////      .filterKeys { it == Direction.south }
////      .filterValues { offset ->
////        !containsConnection(input.general.grid.connections, input.cell, input.cell + offset)
////      }
//      .map { (direction, _) ->
//        Pair(MeshId.arcWall8thB, directionRotation(direction))
//      }
//
//  diagonals.plus(optionalWalls)
////      optionalWalls
//      .map { (mesh, angleZ) ->
//        val facingOffset = Quaternion().rotateZ(angleZ) * Vector3(cellHalfLength - 0.25f, 0f, 0f)
//        WallPlacement(
//            config = input.general.config,
//            mesh = mesh,
//            position = cellCenterOffset + facingOffset,
//            orientation = Quaternion().rotateZ(angleZ + Pi),
//            biome = input.biome
//        )
//      }
//      .map(::newWallInternal)
//}

fun placeWall(general: ArchitectureInput, mesh: MeshName, position: Vector3, direction: Direction, biome: BiomeName): Hand {
  val orientation = getWallOrientation(direction)
  val biomeInfo = general.config.biomes[biome]!!
  return newWallInternal(WallPlacement(general.config, mesh, position, orientation, biomeInfo))
}

fun getCubeWallPosition(direction: Direction): Vector3 {
  val offset = directionVectors[direction]!!
  return offset.toVector3() * cellHalfLength
}

fun cubeWall(input: BiomedBuilderInput, mesh: MeshName, direction: Direction): Hand {
  val general = input.general
  val biome = input.biome
  val position = getCubeWallPosition(direction)
  return placeWall(general, mesh, position, direction, biome.name)
}

fun placeCubeRoomWalls(mesh: MeshName, directions: Collection<Direction>): BiomedBuilder = { input ->
  directions
      .map { direction ->
        cubeWall(input, mesh, direction)
      }
}

fun cubeWalls(mesh: MeshName): BiomedBuilder = { input ->
  placeCubeRoomWalls(mesh, horizontalDirections - input.neighbors)(input)
}

enum class WallFeature {
  lamp,
  window,
  none
}

fun fullWallFeatures() = listOf(WallFeature.lamp, WallFeature.window, WallFeature.none)

fun cubeWallsWithFeatures(
    features: List<WallFeature>,
    mesh: MeshName = MeshId.squareWall,
    offset: Vector3 = Vector3.zero,
    possibleDirections: Set<Direction> = horizontalDirections
): BiomedBuilder = { input ->
  val dice = input.general.dice
  val directions = possibleDirections - input.neighbors
  val featureCount = dice.getInt(min(min(2, directions.size), features.size))
  val featureDirections = dice.take(directions, featureCount)
  val plainDirections = directions - featureDirections
  val selectedFeatures = dice.take(features, featureCount)

  placeCubeRoomWalls(mesh, plainDirections)(input)
      .plus(featureDirections.zip(selectedFeatures) { direction, feature ->
        when (feature) {
          WallFeature.lamp -> listOf(cubeWall(input, mesh, direction), cubeWallLamp(direction, offset))
          WallFeature.window -> listOf(cubeWall(input, MeshId.squareWallWindow, direction))
          WallFeature.none -> listOf(cubeWall(input, mesh, direction))
        }
      }
          .flatten()
      )
}
