package generation.architecture.building

import generation.architecture.engine.*
import generation.general.Direction
import generation.general.directionVectors
import generation.general.horizontalDirections
import generation.general.verticalDirections
import marloth.scenery.enums.MeshId
import silentorb.mythic.spatial.*
import simulation.entities.Depiction
import simulation.main.Hand
import simulation.misc.cellHalfLength
import kotlin.math.min

fun getWallOrientation(direction: Direction): Quaternion {
  val angleZ = directionRotation(direction)
  return Quaternion().rotateZ(angleZ)
}

fun newWallInternal(config: GenerationConfig, depiction: Depiction, position: Vector3, orientation: Quaternion, scale: Vector3 = Vector3.unit): Hand {
  return newArchitectureMesh(
      meshes = config.meshes,
      depiction = depiction,
      position = position,
      orientation = orientation.rotateZ(quarterAngle),
      scale = scale
  )
}

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

fun placeWall(general: ArchitectureInput, depiction: Depiction, position: Vector3, direction: Direction): Hand {
  val orientation = getWallOrientation(direction)
  return newWallInternal(general.config, depiction, position, orientation)
}

fun getCubeWallPosition(direction: Direction): Vector3 {
  val offset = directionVectors[direction]!!
  return offset.toVector3() * cellHalfLength
}

fun cubeWall(input: BuilderInput, depiction: Depiction, offset: Vector3 = Vector3.zero): (Direction) -> Hand = { direction ->
  val general = input.general
  val position = offset + getCubeWallPosition(direction) + Vector3(0f, 0f, -cellHalfLength)
  placeWall(general, depiction, position, direction)
}

fun cubeWall(input: BuilderInput, depiction: Depiction, direction: Direction, offset: Vector3 = Vector3.zero): Hand =
    cubeWall(input, depiction, offset)(direction)

fun placeCubeRoomWalls(depiction: Depiction, directions: Collection<Direction>): Builder = { input ->
  directions
      .map(cubeWall(input, depiction))
}

enum class WallFeature {
  lamp,
  window,
  none
}

fun fullWallFeatures() = listOf(WallFeature.lamp, WallFeature.window, WallFeature.none)

fun cubeWallsWithFeatures(
    features: List<WallFeature>,
    wallDepiction: Depiction,
    offset: Vector3 = Vector3.zero,
    lampOffset: Vector3 = Vector3.zero,
    possibleDirections: Set<Direction> = horizontalDirections
): Builder = { input ->
  val dice = input.general.dice
  val directions = possibleDirections - input.neighbors
  val featureCount = dice.getInt(min(min(2, directions.size), features.size))
  val featureDirections = dice.take(directions, featureCount)
  val plainDirections = directions - featureDirections
  val selectedFeatures = dice.take(features, featureCount)

  placeCubeRoomWalls(wallDepiction, plainDirections)(input)
      .plus(featureDirections.zip(selectedFeatures) { direction, feature ->
        when (feature) {
          WallFeature.lamp -> listOf(cubeWall(input, wallDepiction, direction, offset), cubeWallLamp(direction, offset + lampOffset))
          WallFeature.window -> listOf(cubeWall(input, Depiction(mesh = MeshId.squareWallWindow, texture = wallDepiction.texture), direction, offset))
          WallFeature.none -> listOf(cubeWall(input, wallDepiction, direction, offset))
        }
      }
          .flatten()
      )
}

fun roomWalls(depiction: Depiction) =
    cubeWallsWithFeatures(
        listOf(WallFeature.lamp, WallFeature.none),
        wallDepiction = depiction,
        lampOffset = Vector3(0f, 0f, -1.2f)
    )
