package marloth.clienting.rendering

import silentorb.mythic.fathom.misc.ModelFunction
import silentorb.mythic.lookinglass.ElementGroup
import silentorb.mythic.lookinglass.ElementGroups
import silentorb.mythic.lookinglass.ModelMeshMap
import silentorb.mythic.scenery.Camera
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.toMutableMatrix
import kotlin.math.max

data class CullingContext(
    val location: Vector3,
    val facingNormal: Vector3
)

fun cullElementGroup(meshes: ModelMeshMap, models: Map<String, ModelFunction>, cullingContext: CullingContext): (ElementGroup) -> ElementGroup = { group ->
  val groupMeshes = group.meshes.filter { meshElement ->
    val bounds = meshes[meshElement.mesh]?.bounds ?: models[meshElement.mesh]?.collision
    val meshLocation = Vector3.zero.transform(meshElement.transform)
    val meshRadius = if (bounds != null) {
      val scale = meshElement.transform.getScale()
      bounds.radius * max(max(scale.x, scale.y), scale.z)
    }
    else
      7f

    val sizeOffset = cullingContext.facingNormal * meshRadius
    val a = (meshLocation - (cullingContext.location - sizeOffset)).normalize()
    val dot = a.dot(cullingContext.facingNormal)
    val range = 0.4f
    dot > range
  }
  group.copy(
      meshes = groupMeshes
  )
}

fun getCullingContext(camera: Camera) =
    CullingContext(
        location = camera.position,
        facingNormal = camera.lookAt
    )

fun cullElementGroups(meshes: ModelMeshMap, models: Map<String, ModelFunction>, camera: Camera, groups: ElementGroups): ElementGroups {
//  val previous = groups.sumBy { it.meshes.size }
  val result = groups.map(cullElementGroup(meshes, models, getCullingContext(camera)))
//  val next = result.sumBy { it.meshes.size }
  return result
}
