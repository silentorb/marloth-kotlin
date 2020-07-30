package generation.architecture.building

import generation.architecture.engine.*
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.quarterAngle
import simulation.entities.Depiction
import simulation.misc.cellHalfLength
import simulation.misc.cellLength
import kotlin.math.asin

fun floorMeshBuilder(depiction: Depiction, offset: Vector3 = Vector3.zero, orientation: Quaternion = Quaternion()): Builder = { input ->
  val config = input.general.config
  val location = Vector3(0f, 0f, -cellHalfLength) + align(config.meshes, alignWithCeiling)(depiction.mesh) + offset + Vector3(0f, 0f, 0.01f)
  listOf(newArchitectureMesh(
      meshes = config.meshes,
      depiction = depiction,
      position = location,
      orientation = orientation
  ))
}

fun floorMesh(depiction: Depiction, offset: Vector3 = Vector3.zero, orientation: Quaternion = Quaternion()) =
    floorMeshBuilder(depiction, offset, orientation)

fun diagonalHalfFloorMesh(depiction: Depiction): Builder = { input ->
  val orientation = Quaternion().rotateZ(-quarterAngle)
  floorMeshBuilder(depiction, orientation = orientation)(input)
}
