package lab

import mythic.glowing.DrawMethod
import mythic.spatial.Vector4
import mythic.spatial.identityMatrix
import org.recast4j.detour.NavMesh
import rendering.Renderer
import rendering.drawSolidFace
import rendering.shading.ObjectShaderConfig
import rendering.shading.ShaderFeatureConfig
import simulation.intellect.navigation.originalNavMeshData

fun renderNavMesh(renderer: Renderer, navMesh: NavMesh) {
  val vertices = if (true) {
    (0 until navMesh.tileCount).flatMap { i ->
      val tile = navMesh.getTile(i)
      val data = tile.data
      data.polys.flatMap { poly -> poly.verts.map { data.verts[it] } }
    }
//    for (i in 0 until navMesh.tileCount) {
//      val tile = navMesh.getTile(i)
//      val data = tile.data
//      drawSolidFace(renderer, data.detailTris.map { data.verts[it] }, Vector4(0f, 1f, 1f, 1f))
//    }
  } else {
    originalNavMeshData.flatMap { mesh -> mesh.tris.flatMap { mesh.verts.drop(it * 3).take(3) } }
//    }
  }
  val effect = renderer.getShader(renderer.vertexSchemas.shaded, ShaderFeatureConfig(
//        shading = true
  ))
  effect.activate(ObjectShaderConfig(
      color = Vector4(0f, 1f, 1f, 1f),
      normalTransform = identityMatrix
  ))
  renderer.dynamicMesh.load(vertices)
  renderer.dynamicMesh.draw(DrawMethod.triangles)
}
