package marloth.clienting

import silentorb.mythic.lookinglass.ModelMeshMap
import silentorb.mythic.scenery.Light
import silentorb.mythic.scenery.MeshName

fun gatherMeshLights(meshes: ModelMeshMap): Map<MeshName, List<Light>> {
  return meshes.filter { it.value.lights.any() }
      .mapValues { (_, mesh) ->
        mesh.lights
      }
}
