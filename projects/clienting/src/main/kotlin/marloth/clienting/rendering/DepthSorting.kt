package marloth.clienting.rendering

import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.lookinglass.ElementGroups
import silentorb.mythic.scenery.Camera

fun depthSort(camera: Camera, groups: ElementGroups): ElementGroups {
  return if (getDebugBoolean("RENDER_DEPTH_SORTED")) {
    val prepared = groups.map { group ->
      val transform = group.meshes.firstOrNull()?.transform
      if (transform == null)
        0f to group
      else
        camera.location.distance(transform.translation()) to group
    }
    prepared
        .sortedBy { it.first }
        .map { it.second }
  } else
    groups
}
