package lab.views.world

//import lab.PositionFunction
import mythic.bloom.Bounds
import mythic.drawing.Canvas
import mythic.sculpting.FlexibleMesh
import mythic.spatial.Vector4
import org.joml.xy
import simulation.*

fun drawVertices(bounds: Bounds, getPosition: PositionFunction, canvas: Canvas, mesh: FlexibleMesh) {
  val solid = canvas.solid(Vector4(1f, 0.6f, 0.0f, 1f))
  val lineColor = Vector4(0f, 0f, 1f, 1f)
  for (edge in mesh.edges) {
    val face = edge.faces.filter { getFaceInfo(it).type == FaceType.wall }.firstOrNull()
    if (face != null) {
      val debugInfo = getFaceInfo(face).debugInfo
      if (debugInfo != null) {
        val color = when (debugInfo) {
          "space-a" -> Vector4(1f, 0f, 1f, 1f)
          "space-b" -> Vector4(0f, 1f, 1f, 1f)
          "space-d" -> Vector4(1f, 0f, 0f, 1f)
          else -> Vector4(1f, 1f, 1f, 0.5f)
        }
        canvas.drawLine(getPosition(edge.first.xy), getPosition(edge.second.xy), color, 3f)

        continue
      }
    }
    canvas.drawLine(getPosition(edge.first.xy), getPosition(edge.second.xy), lineColor, 3f)
  }

  for (vertex in mesh.redundantVertices) {
    canvas.drawSolidCircle(getPosition(vertex.xy), 3f, solid)
  }
}

fun drawStructureWorld(bounds: Bounds, getPosition: PositionFunction, canvas: Canvas, world: AbstractWorld) {
  drawVertices(bounds, getPosition, canvas, world.mesh)
}
