package lab.views.world

//import lab.PositionFunction
import mythic.bloom.Bounds
import mythic.drawing.Canvas
import mythic.sculpting.ImmutableEdge
import mythic.sculpting.ImmutableMesh
import mythic.spatial.Vector4
import mythic.typography.TextStyle
import org.joml.plus
import simulation.*

private fun getLineColor(faces: ConnectionTable, edge: ImmutableEdge): Vector4 {
  val wallFaces = edge.faces//.filter { isSolidWall(faces[it.id]!!) }
  val debugInfo = wallFaces.mapNotNull { faces[it.id]?.debugInfo }.firstOrNull()
  if (debugInfo != null) {
    val opacity = 1f//if (faceNodeCount(face) == 2) 1f else 0.1f
    return when (debugInfo) {
      "space-a" -> Vector4(1f, 0f, 1f, opacity)
      "space-b" -> Vector4(0f, 1f, 1f, opacity)
      "space-d" -> Vector4(1f, 0f, 0f, 1f)
      else -> Vector4(1f, 1f, 1f, opacity)
    }
  }
//  val opacity = if (edge.faces.any(isSpace)) 0.2f else 0.5f
  return Vector4(0f, 0f, 1f, 0.5f)
}

fun drawVertices(faces: ConnectionTable, bounds: Bounds, getPosition: PositionFunction, canvas: Canvas, mesh: ImmutableMesh) {
  val solid = canvas.solid(Vector4(1f, 0.6f, 0.0f, 1f))
  val lineColor = Vector4(0f, 0f, 1f, 1f)
  val edges = mesh.edges.values.filter { it.vertices[0].z == it.vertices[1].z && it.vertices[1].z == 0f }
  val style = TextStyle(
      canvas.fonts[0],
      12f,
      Vector4(1f, 1f, 1f, 1f)
  )
  for (edge in edges) {
    val wallFaces = edge.faces.filter { faces[it.id]!!.faceType == FaceType.wall }
//    assert(wallFaces.size < 2)
    val color = getLineColor(faces, edge)
    if (wallFaces.any()) {
      val normal = wallFaces.first().normal
      val middle = edge.middle.xy()
      canvas.drawLine(getPosition(middle), getPosition(middle + normal.xy()), color, 3f * wallFaces.size)
      canvas.drawLine(getPosition(edge.first.xy()), getPosition(edge.second.xy()), color, 3f)
//      canvas.drawText(wallFaces.first().id.toString(),
//          getPosition(middle + normal.xy()),
//          style)
    } else {
      val i = edge.faces.map { faces[it.id] }
      val spaceFaces = edge.faces.filter { faces[it.id]!!.faceType == FaceType.space }
      val c = Vector4(0.5f, 0.5f, 0.5f, 1f)
      if (spaceFaces.any()) {
        val normal = spaceFaces.first().normal
        val middle = edge.middle.xy()
        canvas.drawLine(getPosition(middle), getPosition(middle + normal.xy()), c, 3f * spaceFaces.size)
        canvas.drawLine(getPosition(edge.first.xy()), getPosition(edge.second.xy()), c, 3f)
          }
      else {
        canvas.drawLine(getPosition(edge.first.xy()), getPosition(edge.second.xy()), c, 3f)
      }
    }
  }

  for (vertex in mesh.redundantVertices) {
    canvas.drawSolidCircle(getPosition(vertex.xy()), 3f, solid)
  }
}

fun drawStructureWorld(bounds: Bounds, getPosition: PositionFunction, canvas: Canvas, world: Realm) {
  drawVertices(world.faces, bounds, getPosition, canvas, world.mesh)
}
