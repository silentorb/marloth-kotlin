package generation.architecture.building

import generation.architecture.engine.Builder
import generation.architecture.engine.align
import generation.architecture.engine.alignWithFloor
import generation.architecture.engine.newArchitectureMesh
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import simulation.entities.Depiction

fun treeBuilder(depiction: Depiction, offset: Vector3 = Vector3.zero, orientation: Quaternion = Quaternion()): Builder = { input ->
  val config = input.general.config
  val location = align(config.meshes, alignWithFloor)(depiction.mesh) + offset + Vector3(0f, 0f, -0.2f)
  listOf(newArchitectureMesh(
      meshes = config.meshes,
      depiction = depiction,
      position = location,
      orientation = orientation
  ))
}
