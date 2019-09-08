package lab

import mythic.spatial.Vector4
import org.recast4j.detour.NavMesh
import rendering.Renderer
import rendering.drawSolidFace

fun renderNavMesh(renderer: Renderer, navMesh: NavMesh) {
  for (i in 0 until navMesh.tileCount) {
    val tile = navMesh.getTile(i)
    val data = tile.data
    drawSolidFace(renderer, data.detailTris.map { data.verts[it] }, Vector4(0f, 1f, 1f, 1f))
  }
}
