package lab.views.world

//import lab.PositionFunction
import generation.structure.faceNodeCount
import mythic.bloom.Bounds
import mythic.drawing.Canvas
import mythic.sculpting.FlexibleEdge
import mythic.sculpting.FlexibleFace
import mythic.sculpting.FlexibleMesh
import mythic.spatial.Vector4
import org.joml.plus
import org.joml.xy
import simulation.*

private fun getLineColor(edge: FlexibleEdge): Vector4 {
  val wallFaces = edge.faces.filter(isWall)
  val face = wallFaces.firstOrNull()
  if (face != null) {
    val debugInfo = getFaceInfo(face).debugInfo
    val opacity = if (faceNodeCount(face) == 2) 1f else 0.1f
    if (debugInfo != null) {
      return when (debugInfo) {
        "space-a" -> Vector4(1f, 0f, 1f, opacity)
        "space-b" -> Vector4(0f, 1f, 1f, opacity)
        "space-d" -> Vector4(1f, 0f, 0f, 1f)
        else -> Vector4(1f, 1f, 1f, opacity)
      }
    }
  }

  return Vector4(0f, 0f, 1f, 0.5f)
}

fun drawVertices(bounds: Bounds, getPosition: PositionFunction, canvas: Canvas, mesh: FlexibleMesh) {
  val solid = canvas.solid(Vector4(1f, 0.6f, 0.0f, 1f))
  val lineColor = Vector4(0f, 0f, 1f, 1f)
  val edges = mesh.edges.filter {it.vertices[0].z == it.vertices[1].z && it.vertices[1].z == 0f}
  for (edge in edges) {
    val wallFaces = edge.faces.filter(isWall)
//    assert(wallFaces.size < 2)
    if (wallFaces.any()) {
      val color = getLineColor(edge)
      val normal = wallFaces.first().normal
      val middle = edge.middle.xy
      canvas.drawLine(getPosition(edge.first.xy), getPosition(edge.second.xy), color, 3f)
      canvas.drawLine(getPosition(middle), getPosition(middle + normal.xy), color, 3f * wallFaces.size)
    }
  }

  for (vertex in mesh.redundantVertices) {
    canvas.drawSolidCircle(getPosition(vertex.xy), 3f, solid)
  }
}

fun drawStructureWorld(bounds: Bounds, getPosition: PositionFunction, canvas: Canvas, world: AbstractWorld) {
  drawVertices(bounds, getPosition, canvas, world.mesh)
}
