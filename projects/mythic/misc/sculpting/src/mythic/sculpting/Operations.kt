package mythic.sculpting

import mythic.spatial.*
import org.joml.plus
import org.joml.unaryMinus

typealias Vertices = List<Vector3>
typealias Edges = List<FlexibleEdge>

fun skinLoop(mesh: FlexibleMesh, first: List<Vector3>, second: List<Vector3>) {
  val sides = (0 until first.size).map { a ->
    val b = if (a == first.size - 1) 0 else a + 1
    mesh.createStitchedFace(listOf(
        first[b], first[a],
        second[a], second[b]
    ))
  }
}

fun skin(mesh: FlexibleMesh, first: List<Vector3>, second: List<Vector3>) {
  val sides = (0 until first.size - 1).map { a ->
    val b = a + 1
    mesh.createStitchedFace(
        if (a == 0 && first[a] == first[b])
          listOf(first[a], second[a], second[b])
        else if (second.size == 1) //a == first.size - 1 && second[a] == second[b])
          listOf(first[b], first[a], second[0])
        else
          listOf(first[b], first[a], second[a], second[b])
    )
  }
}

fun extrudeBasic(mesh: FlexibleMesh, face: FlexibleFace, transform: Matrix) {
  val newVertices = face.vertices
      .reversed()
      .map { it.transform(transform) }
  val secondFace = mesh.createFace(newVertices)
  val secondVertices = secondFace.vertices.reversed()
  skinLoop(mesh, face.vertices, secondVertices)
}

fun keepOrRotate(matrix: Matrix, input: Vector3): Vector3 =
    if (input.x == 0f && input.y == 0f)
      input
    else
      input.transform(matrix)

fun lathe(mesh: FlexibleMesh, path: List<Vector3>, count: Int, sweep: Float = Pi * 2) {
  val increment = sweep / count
  var previous = path
  for (i in 1 until count) {
    val matrix = Matrix().rotateZ(i * increment)
    val next = path.map { keepOrRotate(matrix, it) }
    skin(mesh, previous, next)
    previous = next
  }
  skin(mesh, previous, path)
}

//fun interpolatePaths(firstPath: Vertices, secondPath: Vertices, weight: Float): Vertices {
//  val secondIterator = secondPath.iterator()
//  return firstPath.map { secondIterator.next() * weight + it * (1 - weight) }
//}
//
//fun interpolate(first: Float, second: Float, weight: Float)=
//

fun sawRange(x: Float) =
    Math.abs((x % 2) - 1)

fun sineRange(x: Float): Float =
    Math.sin((x * Pi * 2 - Pi * 0.5f).toDouble()).toFloat() * 0.5f + 0.5f

/* 0 >= i >= 1 */
fun bezierSample(i: Float, points: List<Vector2>) {

}

data class SwingInfo(
    val point: Vector3,
    val scale: Vector3
)

fun latheTwoPaths(mesh: FlexibleMesh, firstPath: Vertices, secondPath: Vertices) {
  val sweep: Float = Pi * 2
  val count = 8 * 3
  val increment = sweep / count
  val pivots = cloneVertices(firstPath.intersect(secondPath).filter { it.x == 0f })
  val firstLastPath = firstPath.map {
    val pivot = pivots.firstOrNull { p -> p == it }
    if (pivot != null)
      pivot
    else
      it
  }
  var previous = firstLastPath
  val startVector = Vector3(1f, 0f, 0f)
  val secondIterator = secondPath.iterator()
  val swings = firstPath.map {
    val other = secondIterator.next()
    val shortest = Math.min(it.x, other.x)
    SwingInfo(Vector3(shortest, 0f, it.z), Vector3(it.x, other.x, shortest) / shortest)
  }
  for (i in 1 until count) {
    val angle = i * increment
    val matrix = Matrix().rotateZ(angle)
//    val weight = Math.abs(startVector.transform(matrix).normalize().dot(startVector))
//    val weight = 1 - sawWave(angle / (Pi / 2))
//    val weight = sineRange(angle / Pi)
//    val weight2 = 1 - sawRange(angle / (Pi / 2))
//    val interpolation = interpolatePaths(firstPath, secondPath, weight)
    val next = swings.map {
      val pivot = pivots.firstOrNull { p -> p == it.point }
      if (pivot != null)
        pivot
      else
        it.point.transform(matrix) * it.scale
    }
    skin(mesh, previous, next)
    previous = next
  }

  skin(mesh, previous, firstLastPath)
}

fun translate(matrix: Matrix, vertices: List<Vector3>) {
//  vertices.forEach { it.set(it.transform(matrix)) }
  for (vertex in vertices) {
    vertex.set(vertex.transform(matrix))
  }
}

fun translatePosition(offset: Vector3, vertices: List<Vector3>) {
  translate(Matrix().translate(offset), vertices)
}

//fun convertPath(path: Vertices) =
//    path.map { Vector3(it.x, 0f, it.y) }

//fun lathe(mesh: FlexibleMesh, arc: Vertices, horizontalCount: Int) {
//  lathe(mesh, convertPath(arc), horizontalCount)
//}

fun alignToFloor(vertices: List<Vector3>, floor: Float = 0f) {
  val lowest = vertices.map { it.z }.sorted().first()
  translatePosition(Vector3(0f, 0f, floor - lowest), vertices)
}

fun alignToCeiling(vertices: List<Vector3>, ceiling: Float = 0f) {
  val highest = vertices.map { it.z }.sorted().last()
  translatePosition(Vector3(0f, 0f, ceiling - highest), vertices)
}

data class VerticalDimensions(
    val top: Float,
    val bottom: Float,
    val height: Float = top - bottom
)

fun getPathDimensions(path: Vertices): VerticalDimensions {
  val sorted = path.map { it.y }.sorted()
  val bottom = sorted.first()
  val top = sorted.last()
  return VerticalDimensions(top, bottom)
}

fun cloneVertices(vertices: Collection<Vector3>): Vertices =
    vertices.map { Vector3(it) }

fun flipVertical(vertices: Vertices): Vertices {
  val middle = vertices.map { it.z }.average().toFloat()
  return vertices.map { Vector3(it.x, it.y, middle - (it.z - middle)) }
}

fun joinPaths(verticalGap: Float, first: Vertices, second: Vertices): Vertices {
  val firstCopy = cloneVertices(first)
  val secondCopy = cloneVertices(second)
  val half = verticalGap * 2
  alignToFloor(firstCopy, half)
  alignToCeiling(secondCopy, -half)
  return firstCopy.plus(secondCopy)
}

fun convertAsXZ(vertices: List<Vector2>) =
    vertices.map { Vector3(it.x, 0f, it.y) }

fun setAnchor(anchor: Vector3, vertices: Vertices) {
  translatePosition(-anchor, vertices)
}