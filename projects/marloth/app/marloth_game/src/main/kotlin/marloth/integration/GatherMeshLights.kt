package marloth.integration

import silentorb.mythic.lookinglass.ModelMeshMap
import simulation.misc.LightAttachmentMap

fun gatherMeshLights(meshes: ModelMeshMap): LightAttachmentMap {
  return meshes.filter { it.value.lights.any() }
      .mapValues { (_, mesh) ->
        mesh.lights
      }
}
