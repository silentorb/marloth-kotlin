package lab

import generation.StructureWorld
import mythic.bloom.Bounds
import mythic.drawing.Canvas
import mythic.sculpting.HalfEdgeMesh
import mythic.spatial.Vector4
import org.joml.xy

fun drawVertices(bounds: Bounds, getPosition: PositionFunction, canvas: Canvas, mesh: HalfEdgeMesh) {
  val solid = canvas.solid(Vector4(1f, 0.6f, 0.7f, 1f))
  for (vertex in mesh.vertices) {
    canvas.drawSolidCircle(getPosition(vertex.position.xy), 3f, solid)
  }
}

fun drawStructureWorld(bounds: Bounds, getPosition: PositionFunction, canvas: Canvas, world: StructureWorld) {
  drawVertices(bounds, getPosition, canvas, world.mesh)
}
