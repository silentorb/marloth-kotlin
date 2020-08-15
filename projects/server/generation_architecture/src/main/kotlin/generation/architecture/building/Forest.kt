package generation.architecture.building

import generation.architecture.engine.Builder
import generation.architecture.engine.newArchitectureMesh
import marloth.scenery.enums.MeshId
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import simulation.entities.Depiction
import simulation.misc.cellHalfLength

fun prop(depiction: Depiction, location: Vector3 = Vector3.zero, orientation: Quaternion = Quaternion()): Builder = { input ->
  val config = input.general.config
  listOf(newArchitectureMesh(
      meshes = config.meshes,
      depiction = depiction,
      position = location,
      orientation = orientation
  ))
}

fun lampPostBuilder(offset: Vector3 = Vector3.zero, orientation: Quaternion = Quaternion()): Builder = { input ->
  val config = input.general.config
  val location = offset + Vector3(0f, 0f, -cellHalfLength)
  listOf(newArchitectureMesh(
      meshes = config.meshes,
      depiction = Depiction(mesh = MeshId.lampPost),
      position = location,
      orientation = orientation
  ))
}
