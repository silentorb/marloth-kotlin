package mythic.sculpting

import mythic.spatial.*
import kotlin.math.cos
import kotlin.math.sin

fun createArc(radius: Float, count: Int, sweep: Float = Pi * 2, offset: Float = 0f): Vertices {
  val vertices = ArrayList<Vector3>(count)
  val increment = sweep / (count - 1)

  for (i in 0 until count) {
    val theta = increment * i + offset
    vertices.add(Vector3(cos(theta) * radius, 0f, sin(theta) * radius))
  }
  if (sweep == Pi)
    vertices.last().y = 0f

  return vertices
}

fun createCircle(mesh: FlexibleMesh, radius: Float, count: Int): FlexibleFace {
  return mesh.createFace(createArc(radius, count))
}

fun createArc2(radius: Float, count: Int, sweep: Float = Pi * 2): Vertices {
  val vertices = ArrayList<Vector3>(count)
  val increment = sweep / (count)

  for (i in 0 until count) {
    val theta = increment * i
    vertices.add(Vector3(sin(theta) * radius, cos(theta) * radius, 0f))
  }
  if (sweep == Pi)
    vertices.last().x = 0f

  return vertices
}

fun createCircle2(mesh: FlexibleMesh, radius: Float, count: Int): FlexibleFace {
  return mesh.createFace(createArc2(radius, count))
}

//fun createIncompleteCircle(mesh: FlexibleMesh, radius: Float, count: Int, take: Int): FlexibleFace {
//  return mesh.createFace(convertPath(createArc(radius, count)).take(take))
//}

fun createCylinder(mesh: FlexibleMesh, radius: Float, count: Int, length: Float) {
  val circle = createCircle2(mesh, radius, count)
  extrudeBasic(mesh, circle, Matrix().translate(Vector3(0f, 0f, length)))
}

fun createSphere(mesh: FlexibleMesh, radius: Float, horizontalCount: Int, verticalCount: Int) =
    lathe(mesh, createArc(radius, verticalCount, Pi, -Pi / 2), horizontalCount)
//    mesh.createFace(createArc(radius, verticalCount, Pi, -Pi / 2))

fun createIncompleteSphere(mesh: FlexibleMesh, radius: Float, horizontalCount: Int, verticalCount: Int, take: Int) =
    lathe(mesh, createArc(radius, verticalCount, Pi).take(take), horizontalCount)

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

//fun createLines(mesh: FlexibleMesh, path: Vertices) {
//  for (i in 0 until path.size - 1) {
//    mesh.createEdge(path[i], path[i + 1])
//  }
//}