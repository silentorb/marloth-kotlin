package lab.views

//import lab.PositionFunction
import mythic.bloom.Bounds
import mythic.drawing.Canvas
import mythic.sculpting.HalfEdgeMesh
import mythic.spatial.Vector4
import org.joml.xy
import simulation.*

fun drawVertices(bounds: Bounds, getPosition: PositionFunction, canvas: Canvas, mesh: HalfEdgeMesh) {
  val solid = canvas.solid(Vector4(1f, 0.6f, 0.0f, 1f))
  val lineColor = Vector4(0f, 0f, 1f, 1f)
  for (edge in mesh.edges) {
    canvas.drawLine(getPosition(edge.vertex.position.xy), getPosition(edge.next!!.vertex.position.xy), lineColor, 3f)
  }

  for (vertex in mesh.vertices) {
    canvas.drawSolidCircle(getPosition(vertex.position.xy), 3f, solid)
  }
}

fun drawStructureWorld(bounds: Bounds, getPosition: PositionFunction, canvas: Canvas, world: StructureWorld) {
  drawVertices(bounds, getPosition, canvas, world.mesh)
}
