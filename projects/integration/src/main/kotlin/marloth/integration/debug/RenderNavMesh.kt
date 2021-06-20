package marloth.integration.debug

import silentorb.mythic.glowing.DrawMethod
import silentorb.mythic.glowing.drawMesh
import silentorb.mythic.glowing.globalState
import silentorb.mythic.spatial.Matrix
import silentorb.mythic.spatial.Vector4
import org.recast4j.detour.NavMesh
import org.recast4j.recast.Heightfield
import org.recast4j.recast.Span
import silentorb.mythic.lookinglass.Renderer
import silentorb.mythic.lookinglass.shading.ObjectShaderConfig
import silentorb.mythic.lookinglass.shading.ShaderFeatureConfig
import marloth.scenery.enums.MeshId
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.lookinglass.SceneRenderer
import silentorb.mythic.spatial.Vector3
import simulation.intellect.navigation.globalHeightMap
import simulation.intellect.navigation.originalNavMeshData
import kotlin.math.abs

fun renderNavMeshVoxels(renderer: SceneRenderer, hf: Heightfield) {
  val orig = hf.bmin
  val cs = hf.cs
  val ch = hf.ch

  val w = hf.width
  val h = hf.height

  val cube = renderer.meshes[MeshId.cube]!!
  val effect = renderer.getShader(renderer.renderer.vertexSchemas.flat, ShaderFeatureConfig())
  globalState.depthEnabled = true
  for (y in 0 until h) {
    for (x in 0 until w) {
      val fx = orig[0] + x * cs
      val fz = orig[2] + y * cs
      var s: Span? = hf.spans[x + y * w]
      while (s != null) {
//        appendBox(fx, orig[1] + s.smin * ch, fz, fx + cs, orig[1] + s.smax * ch, fz + cs, fcol)
        val height = s.smax - s.smin
        val location = Vector3(
            fx + cs / 2f,
            fz + cs / 2f,
            orig[1] + s.smin * ch + height * ch / 2f
        )
        if (location.distance(renderer.camera.position) < 10f) {
          effect.activate(ObjectShaderConfig(
              color = Vector4(0.2f, abs(fx + fz + orig[1]) % 1f, 0.3f, 0.6f),
              transform = Matrix.identity
                  .translate(location)
                  .scale(
                      cs,
                      cs,
                      ch// * height
                  )

          ))
          drawMesh(cube.primitives.first().mesh, DrawMethod.triangleFan)
        }
        s = s.next
      }
    }
  }
  globalState.depthEnabled = false
}

private fun renderOutlinedFaces(renderer: Renderer, polygons: List<List<Float>>, solidColor: Vector4, lineColor: Vector4) {
  val effect = renderer.getShader(renderer.vertexSchemas.flat, ShaderFeatureConfig())
  val solidBrush = ObjectShaderConfig(
      color = solidColor
  )
  val lineBrush = ObjectShaderConfig(
      color = lineColor
  )

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
}

fun renderNavMesh(renderer: SceneRenderer, navMesh: NavMesh) {
  if (getDebugBoolean("DRAW_NAV_MESH_VOXELS")) {
    if (globalHeightMap != null)
      renderNavMeshVoxels(renderer, globalHeightMap!!)
  }

  if (getDebugBoolean("DRAW_NAV_MESH")) {
    val polygons = (0 until navMesh.tileCount)
        .flatMap { i ->
          val tile = navMesh.getTile(i)
          val data = tile.data
          data.polys.map { poly ->
            poly.verts
                .take(poly.vertCount)
                .reversed()
                .flatMap {
                  val temp = data.verts.drop(it * 3).take(3)
                  listOf(temp[0], temp[2], temp[1])
                }
          }
        }
    renderOutlinedFaces(renderer.renderer, polygons, Vector4(0.2f, 0.6f, 0.8f, 0.3f), Vector4(0f, 1f, 1f, 0.5f))
  }

  if (getDebugBoolean("DRAW_NAV_INPUT")) {
    val polygons = originalNavMeshData.flatMap { mesh ->
      (mesh.tris.indices step 3)
          .mapNotNull { i ->
            val points = mesh.tris.drop(i).take(3)
                .flatMap {
              val temp = mesh.verts.drop(it * 3).take(3)
              listOf(temp[0], temp[2], temp[1])
            }
            if (Vector3(points[0], points[1], points[2]).distance(renderer.camera.position) < 10f)
              points
            else
              null
          }
    }
    renderOutlinedFaces(renderer.renderer, polygons, Vector4(0.8f, 0.6f, 0.2f, 0.3f), Vector4(1f, 0f, 1f, 0.5f))
  }
}
