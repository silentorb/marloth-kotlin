package generation.architecture.building

import generation.architecture.engine.*
import marloth.scenery.enums.MeshId
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import simulation.entities.Depiction
import simulation.misc.cellHalfLength

fun floorMeshBuilder(depiction: Depiction, offset: Vector3 = Vector3.zero, orientation: Quaternion = Quaternion()): Builder = { input ->
  val config = input.general.config
  val heightFix = if(depiction.mesh == MeshId.floorSquare)
    -0.5f
  else
    0f

  val location = Vector3(0f, 0f, -cellHalfLength) + offset + Vector3(0f, 0f, 0.01f + heightFix)
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
  floorMeshBuilder(depiction)(input)
}
