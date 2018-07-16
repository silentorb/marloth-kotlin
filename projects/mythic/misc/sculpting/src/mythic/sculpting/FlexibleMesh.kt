package mythic.sculpting

import mythic.spatial.Vector3
import mythic.spatial.times
import org.joml.minus
import org.joml.plus

class FlexibleEdge(
    var first: Vector3,
    var second: Vector3,
    val faces: MutableList<FlexibleFace>
) {
  val vertices = listOf(first, second)

  val middle: Vector3
    get() = (first + second) * 0.5f

  fun getReference(face: FlexibleFace) = face.edges.first { it.edge == this }

  val references: List<EdgeReference>
    get() = faces.map { getReference(it) }

  fun matches(a: Vector3, b: Vector3): Boolean =
      (first == a && second == b)
          || (first == b && second == a)
}

class EdgeReference(
    val edge: FlexibleEdge,
    var next: EdgeReference?,
    var previous: EdgeReference?,
    var direction: Boolean
) {
  val vertices: List<Vector3>
    get() = if (direction) edge.vertices else listOf(edge.second, edge.first)

  val faces: List<FlexibleFace>
    get() = edge.faces

  val first: Vector3
    get() = if (direction) edge.first else edge.second

  val second: Vector3
    get() = if (direction) edge.second else edge.first

  val otherEdgeReferences: List<EdgeReference>
    get() = edge.references.filter { it != this }

  val middle: Vector3
    get() = edge.middle

}

fun getNormal(vertices: Vertices) =
    (vertices[2] - vertices[1]).cross(vertices[0] - vertices[1]).normalize()

class FlexibleFace(
    var edges: MutableList<EdgeReference> = mutableListOf(),
    var data: Any? = null
) {
  val unorderedVertices: List<Vector3>
    get() = edges.map { it.first }
//  get() = edges.flatMap { it.vertices }.distinct()

  val vertices: List<Vector3>
    get() = edges.map { it.first }

  var normal = Vector3()

  fun updateNormal() {
//    if (vertices.size > 2)
    normal = getNormal(unorderedVertices)
  }

  val neighbors: List<FlexibleFace>
    get() = edges.flatMap {
      it.faces
    }
        .filter { it !== this }

  fun edge(first: Vector3, second: Vector3): EdgeReference? =
      edges.firstOrNull { it.edge.matches(first, second) }

  fun flipQuad() {
    edges.forEach {
      val a = it.next
      it.next = it.previous
      it.previous = a
      it.direction = !it.direction
    }

    edges = listOf(
        edges[0],
        edges[3],
        edges[2],
        edges[1]
    ).toMutableList()
  }
}

fun distinctVertices(vertices: Vertices) =
    vertices.distinctBy { System.identityHashCode(it) }

class FlexibleMesh {
  //  val vertices: MutableList<Vector3> = mutableListOf()
  val edges: MutableList<FlexibleEdge> = mutableListOf()
  val faces: MutableList<FlexibleFace> = mutableListOf()

  val redundantVertices: Vertices
    get() = edges.flatMap { it.vertices }

//  val distinctVertices: List<Vector3>
//    get() = redundantVertices.distinct()

  val distinctVertices: Vertices
    get() = distinctVertices(redundantVertices)

  fun createFace(): FlexibleFace {
    val face = FlexibleFace()
    faces.add(face)
    return face
  }

  fun createFace(vertices: List<Vector3>): FlexibleFace {
    assert(vertices.distinct().size == vertices.size) // Check for duplicate vertices
    val face = createFace()
    replaceFaceVertices(face, vertices)
    return face
  }

  fun createStitchedFace(vertices: List<Vector3>): FlexibleFace {
    val face = createFace(vertices)
    face.updateNormal()
//    stitchEdges(face.edges)
    return face
  }

  fun getMatchingEdges(first: Vector3, second: Vector3) =
      edges.filter { it.matches(first, second) }

  fun createEdge(first: Vector3, second: Vector3, face: FlexibleFace?): EdgeReference {
    val faces = if (face == null) mutableListOf() else mutableListOf(face)
    val existingEdges = getMatchingEdges(first, second)
    assert(existingEdges.size < 2)
    if (existingEdges.any()) {
      val edge = existingEdges.first()
      if (face != null)
        edge.faces.add(face)

      return EdgeReference(edge, null, null, edge.first == first)
    } else {
      val edge = FlexibleEdge(first, second, faces)
      edges.add(edge)
      return EdgeReference(edge, null, null, true)
    }

  }

  fun createEdges(vertices: Vertices) {
    var previous = vertices.first()
    var previousEdge: EdgeReference? = null
    for (next in vertices.drop(1)) {
      val edge = createEdge(previous, next, null)
      if (previousEdge != null) {
        edge.previous = previousEdge
        previousEdge.next = edge
      }
      previousEdge = edge
      previous = next
    }
  }

  fun replaceFaceVertices(face: FlexibleFace, initializer: List<Vector3>) {
    var previous = initializer.first()
    var previousEdge: EdgeReference? = null
    for (next in initializer.drop(1)) {
      val edge = createEdge(previous, next, face)
      face.edges.add(edge)
      if (previousEdge != null) {
        edge.previous = previousEdge
        previousEdge.next = edge
      }
      previousEdge = edge
      previous = next
    }
    val first = face.edges.first()
    val last = createEdge(initializer.last(), initializer.first(), face)
    face.edges.add(last)
    last.next = first
    previousEdge!!.next = last
    last.previous = previousEdge
    first.previous = last
    assert(face.edges.none { it.next == null || it.previous == null })
  }

  fun sharedImport(mesh: FlexibleMesh) {
    faces.addAll(mesh.faces)
    edges.addAll(mesh.edges)
  }

  fun sharedImport(meshes: List<FlexibleMesh>) {
    meshes.forEach { sharedImport(it) }
  }

}

fun calculateNormals(mesh: FlexibleMesh) {
  for (face in mesh.faces) {
    face.updateNormal()
  }
}
