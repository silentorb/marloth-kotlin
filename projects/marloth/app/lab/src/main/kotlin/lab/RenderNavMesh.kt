package lab

import mythic.glowing.DrawMethod
import mythic.glowing.globalState
import mythic.spatial.Vector4
import mythic.spatial.identityMatrix
import org.recast4j.detour.NavMesh
import rendering.Renderer
import rendering.drawSolidFace
import rendering.shading.ObjectShaderConfig
import rendering.shading.ShaderFeatureConfig
import simulation.intellect.navigation.originalNavMeshData

fun renderNavMesh(renderer: Renderer, navMesh: NavMesh) {
  val effect = renderer.getShader(renderer.vertexSchemas.flat, ShaderFeatureConfig())
  val solidBrush = ObjectShaderConfig(
      color = Vector4(0.2f, 0.6f, 0.8f, 0.3f)
  )
  val lineBrush = ObjectShaderConfig(
      color = Vector4(0f, 1f, 1f, 1f)
  )
  val polygons: List<List<Float>> = if (true) {
    (0 until navMesh.tileCount).flatMap { i ->
      val tile = navMesh.getTile(i)
      val data = tile.data
      data.polys.map { poly ->
        poly.verts.take(poly.vertCount).flatMap {
          val temp = data.verts.drop(it * 3).take(3)
          listOf(temp[0], temp[2], temp[1])
        }
      }
    }
  } else {
    originalNavMeshData.flatMap { mesh ->
      (mesh.tris.indices step 3).map { i ->
        mesh.tris.drop(i).take(3).flatMap {
          val temp = mesh.verts.drop(it * 3).take(3)
          listOf(temp[0], temp[2], temp[1])
        }
      }
//      mesh.tris.flatMap {
//        val temp = mesh.verts.drop(it * 3).take(3)
//        listOf(temp[0], temp[2], temp[1])
//      }
    }
  }

  globalState.depthEnabled = false
  effect.activate(solidBrush)
  for (polygon in polygons) {
    renderer.dynamicMesh.load(polygon)
    renderer.dynamicMesh.draw(DrawMethod.triangleFan)
  }

  effect.activate(lineBrush)
  for (polygon in polygons) {
    renderer.dynamicMesh.load(polygon.plus(polygon.take(3)))
    renderer.dynamicMesh.draw(DrawMethod.lineStrip)
  }

//  globalState.depthEnabled = true
//  renderer.dynamicMesh.load(vertices)
//  renderer.getShader(renderer.vertexSchemas.shaded, ShaderFeatureConfig()).activate(ObjectShaderConfig(
//      color = Vector4(0.2f, 0.6f, 0.8f, 0.3f)
//  ))
//  renderer.dynamicMesh.draw(DrawMethod.triangles)
//  renderer.getShader(renderer.vertexSchemas.shaded, ShaderFeatureConfig()).activate(ObjectShaderConfig(
//      color = Vector4(0f, 1f, 1f, 1f)
//  ))
//  renderer.dynamicMesh.draw(DrawMethod.lineStrip)
}
