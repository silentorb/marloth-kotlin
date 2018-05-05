package mythic.sculpting

import mythic.spatial.*
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
//          listOf(first[a], second[a], second[b])
          listOf(second[b], second[a], first[a])
        else if (second.size == 1) //a == first.size - 1 && second[a] == second[b])
//          listOf(first[b], first[a], second[0])
          listOf(second[0], first[a], first[b])
        else
//          listOf(first[b], first[a], second[a], second[b])
          listOf(second[b], second[a], first[a], first[b])
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

inline fun nearly(value: Float, target: Float) =
    value < target + 0.000001f
        && value > target - 0.000001f

fun keepOrRotate(matrix: Matrix, input: Vector3): Vector3 =
    if (nearly(input.x, 0f) && nearly(input.y, 0f))
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

typealias Swings = List<SwingInfo>

fun createSwings(firstPath: Vertices, secondPath: Vertices): Swings {
  val secondIterator = secondPath.iterator()
  return firstPath.map {
    val other = secondIterator.next()
    val shortest = Math.min(it.x, other.x)
    SwingInfo(Vector3(shortest, 0f, it.z), Vector3(it.x, other.x, shortest) / shortest)
  }
}

fun mapPivots(path: Vertices, pivots: Vertices) =
    path.map {
      val pivot = pivots.firstOrNull { p -> p == it }
      if (pivot != null)
        pivot
      else
        it
    }

fun transformSwing(pivots: Vertices, matrix: Matrix, swing: SwingInfo): Vector3 {
  val pivot = pivots.firstOrNull { p -> p == swing.point }
  return if (pivot != null)
    pivot
  else
    swing.point.transform(matrix) * swing.scale
}

data class LatheCourse(
    val stepCount: Int,
    val transformer: (Int) -> Matrix,
    val wrap: Boolean
)

fun createLatheCourse(resolution: Int, sweep: Float = Pi * 2): LatheCourse {
  val count = (resolution * sweep / (Pi / 2)).toInt() + 1
  val increment = sweep / (count - 1)
  return LatheCourse(
      count,
      { i: Int -> Matrix().rotateZ(i * increment) },
      sweep == Pi * 2
  )
}

fun latheTwoPaths(mesh: FlexibleMesh, latheCourse: LatheCourse, firstPath: Vertices, secondPath: Vertices) {

  val pivots = cloneVertices(firstPath.intersect(secondPath).filter { it.x == 0f })
  val firstLastPath = mapPivots(firstPath, pivots)
  var previous = firstLastPath
  val swings = createSwings(firstPath, secondPath)
  for (i in 1 until latheCourse.stepCount) {
    val matrix = latheCourse.transformer(i)
    val next = swings.map { transformSwing(pivots, matrix, it) }
    skin(mesh, previous, next)
    previous = next
  }

  if (latheCourse.wrap)
    skin(mesh, previous, firstLastPath)
}

fun transformVertices(matrix: Matrix, vertices: Vertices): Vertices {
  for (vertex in vertices) {
    vertex.set(vertex.transform(matrix))
  }
  return vertices
}

//fun transformVertices2D(matrix: Matrix, vertices: List<Vector2>) {
////  vertices.forEach { it.set(it.transform(matrix)) }
//  for (vertex in vertices) {
//    vertex.set(vertex.transform(matrix))
//  }
//}

fun distortedTranslatePosition(offset: Vector3, vertices: List<Vector3>) {
  transformVertices(Matrix().translate(offset), vertices)
}

fun distortedTranslatePosition(offset: Vector3, mesh: FlexibleMesh) {
  transformVertices(Matrix().translate(offset), mesh.redundantVertices)
}

fun transformMesh(mesh: FlexibleMesh, matrix: Matrix) {
  transformVertices(matrix, mesh.distinctVertices)
}

fun translateMesh(mesh: FlexibleMesh, offset: Vector3) {
  transformVertices(Matrix().translate(offset), mesh.distinctVertices)
}

//fun convertPath(path: Vertices) =
//    path.map { Vector3(it.x, 0f, it.y) }

//fun lathe(mesh: FlexibleMesh, arc: Vertices, horizontalCount: Int) {
//  lathe(mesh, convertPath(arc), horizontalCount)
//}

fun alignToFloor(vertices: List<Vector3>, floor: Float = 0f) {
  val lowest = vertices.map { it.z }.sorted().first()
  distortedTranslatePosition(Vector3(0f, 0f, floor - lowest), vertices)
}

fun alignToFloor(mesh: FlexibleMesh, floor: Float = 0f) {
  alignToFloor(mesh.distinctVertices, floor)
}

fun alignToCeiling(vertices: List<Vector3>, ceiling: Float = 0f) {
  val highest = vertices.map { it.z }.sorted().last()
  distortedTranslatePosition(Vector3(0f, 0f, ceiling - highest), vertices)
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
  distortedTranslatePosition(-anchor, vertices)
}

fun stitchEdges(a: FlexibleEdge, b: FlexibleEdge) {
  a.edges.add(b)
  b.edges.add(a)
  b.first = a.second
  b.second = a.first
  b.next!!.first = b.second
  b.previous!!.second = b.first
}

fun stitchEdgeLoops(firstLoop: List<FlexibleEdge>, secondLoop: List<FlexibleEdge>) {
  val secondIterator = secondLoop.listIterator()

  for (a in firstLoop) {
    val b = secondIterator.next()
    stitchEdges(a, b)
//    break
  }
}

fun mirrorAlongY(mesh: FlexibleMesh): List<FlexibleFace> {
  val newFaces = mesh.faces.toList().map { original ->
    mesh.createFace(original.vertices.map { Vector3(it.x, -it.y, it.z) }.reversed())
  }

  return newFaces
}