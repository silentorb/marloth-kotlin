package lab

import mythic.glowing.DrawMethod
import mythic.glowing.drawMesh
import mythic.glowing.globalState
import mythic.spatial.Matrix
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import mythic.spatial.identityMatrix
import org.recast4j.detour.NavMesh
import org.recast4j.recast.Heightfield
import org.recast4j.recast.Span
import randomly.Dice
import rendering.Renderer
import rendering.drawSolidFace
import rendering.shading.ObjectShaderConfig
import rendering.shading.ShaderFeatureConfig
import scenery.enums.MeshId
import simulation.intellect.navigation.globalHeightMap
import simulation.intellect.navigation.originalNavMeshData
import kotlin.math.abs

fun renderNavMeshVoxels(renderer: Renderer, hf: Heightfield) {
  val orig = hf.bmin
  val cs = hf.cs
  val ch = hf.ch

  val w = hf.width
  val h = hf.height

  val cube = renderer.meshes[MeshId.cube.name]!!
  val effect = renderer.getShader(renderer.vertexSchemas.flat, ShaderFeatureConfig())
  globalState.depthEnabled = true
  for (y in 0 until h) {
    for (x in 0 until w) {
      val fx = orig[0] + x * cs
      val fz = orig[2] + y * cs
      var s: Span? = hf.spans[x + y * w]
      while (s != null) {
//        appendBox(fx, orig[1] + s.smin * ch, fz, fx + cs, orig[1] + s.smax * ch, fz + cs, fcol)
        val height = s.smax - s.smin
        effect.activate(ObjectShaderConfig(
            color = Vector4(0.2f, abs(fx + fz + orig[1]) % 1f, 0.3f, 0.6f),
            transform = Matrix()
                .translate(
                    fx + cs / 2f,
                    fz + cs / 2f,
                    orig[1] + s.smin * ch + height * ch / 2f
                )
                .scale(
                    cs,
                    cs,
                    ch
                )

        ))
        drawMesh(cube.primitives.first().mesh, DrawMethod.triangleFan)
        s = s.next
      }
    }
  }
  globalState.depthEnabled = false
}

fun renderNavMesh(renderer: Renderer, navMesh: NavMesh) {
  if (true) {
    if (globalHeightMap != null)
      renderNavMeshVoxels(renderer, globalHeightMap!!)
//    return
  }
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
