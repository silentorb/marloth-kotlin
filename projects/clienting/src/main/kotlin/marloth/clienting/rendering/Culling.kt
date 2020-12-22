package marloth.clienting.rendering

import silentorb.mythic.lookinglass.ElementGroup
import silentorb.mythic.lookinglass.ElementGroups
import silentorb.mythic.lookinglass.ModelMeshMap
import silentorb.mythic.scenery.Camera
import silentorb.mythic.spatial.Vector3
import kotlin.math.max
import kotlin.math.sqrt

data class CullingContext(
    val location: Vector3,
    val facingNormal: Vector3
)

fun cullElementGroup(meshes: ModelMeshMap, cullingContext: CullingContext): (ElementGroup) -> ElementGroup = { group ->
  val groupMeshes = group.meshes.filter { meshElement ->
    val bounds = meshes[meshElement.mesh]?.bounds
    val meshLocation = Vector3.zero.transform(meshElement.transform)
    val meshRadius = if (bounds != null) {
      val scale = meshElement.transform.getScale()
      val length = bounds.radius * max(max(scale.x, scale.y), scale.z)
      sqrt(length * length + length * length)
    }
    else
      7f

    val sizeOffset = cullingContext.facingNormal * meshRadius
    val a = ((meshLocation + sizeOffset) - cullingContext.location).normalize()
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

fun cullElementGroups(meshes: ModelMeshMap, camera: Camera, groups: ElementGroups): ElementGroups {
//  val previous = groups.sumBy { it.meshes.size }
  val result = groups.map(cullElementGroup(meshes, getCullingContext(camera)))
//  val next = result.sumBy { it.meshes.size }
  return result
}
