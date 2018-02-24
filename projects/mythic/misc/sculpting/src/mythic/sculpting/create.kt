package mythic.sculpting

import mythic.spatial.*
import kotlin.math.cos
import kotlin.math.sin

fun createArc(radius: Float, count: Int, sweep: Float = Pi * 2): List<Vector3> {
  val vertices = ArrayList<Vector3>(count)
  val increment = sweep / count

  for (i in 0..count) {
    val theta = increment * i
    vertices.add(Vector3(sin(theta) * radius, cos(theta) * radius, 0f))
  }
  if (sweep == Pi)
    vertices.last().x = 0f

  return vertices
}

fun createCircle(mesh: FlexibleMesh, radius: Float, count: Int): FlexibleFace {
  return mesh.createFace(createArc(radius, count))
}

fun createCylinder(mesh: FlexibleMesh, radius: Float, count: Int, height: Float) {
  val circle = createCircle(mesh, radius, count)
  extrudeBasic(mesh, circle, Matrix().translate(Vector3(0f, 0f, height)))
}

fun createSphere(mesh: FlexibleMesh, radius: Float, horizontalCount: Int, verticalCount: Int) {
  val arc = createArc(radius, verticalCount, Pi)
      .map { Vector3(it.x, 0f, it.y) }

  lathe(mesh, arc, horizontalCount)
//  mesh.createFace(arc)
}

class create {
  companion object {

    fun flatTest(): HalfEdgeMesh {
      val mesh = HalfEdgeMesh()
      mesh.add_face(listOf(
          HalfEdgeVertex(Vector3(1f, 1f, 0f)),
          HalfEdgeVertex(Vector3(0.5f, 1f, 0f)),
          HalfEdgeVertex(Vector3(1f, 0.5f, 0f))
      ))

      mesh.add_face(listOf(
          HalfEdgeVertex(Vector3(-1f, -1f, 0f)),
          HalfEdgeVertex(Vector3(-1f, -0.5f, 0f)),
          HalfEdgeVertex(Vector3(-0.5f, -1f, 0f))
      ))
      return mesh
    }
  }
}

fun createCube(mesh: FlexibleMesh, size: Vector3): List<FlexibleFace> {
  val half = size * 0.5f
  val top = squareUp(mesh, Vector2(size.x, size.y), half.z)
  val bottom = squareDown(mesh, Vector2(size.x, size.y), -half.z)

  val top_vertices = top.vertices
  val initial_bottom_vertices = bottom.vertices
  val bottom_vertices = listOf(
      initial_bottom_vertices[0],
      initial_bottom_vertices[3],
      initial_bottom_vertices[2],
      initial_bottom_vertices[1]
  )

  val sides = (0..3).map { a ->
    val b = if (a > 2) 0 else a + 1
    mesh.createStitchedFace(listOf(
        top_vertices[b], top_vertices[a],
        bottom_vertices[a], bottom_vertices[b]
    ))
  }
  return listOf(top, bottom)
      .plus(listOf())
}

fun squareDown(mesh: FlexibleMesh, size: Vector2, z: Float): FlexibleFace {
  val half = size * 0.5f;
  return mesh.createStitchedFace(listOf(
      Vector3(-half.x, -half.y, z),
      Vector3(-half.x, half.y, z),
      Vector3(half.x, half.y, z),
      Vector3(half.x, -half.y, z)
  ))
}

fun squareUp(mesh: FlexibleMesh, size: Vector2, z: Float): FlexibleFace {
  val half = size * 0.5f;
  return mesh.createStitchedFace(listOf(
      Vector3(-half.x, -half.y, z),
      Vector3(half.x, -half.y, z),
      Vector3(half.x, half.y, z),
      Vector3(-half.x, half.y, z)
  ))
}
