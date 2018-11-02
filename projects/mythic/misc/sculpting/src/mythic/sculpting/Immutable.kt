package mythic.sculpting

import mythic.spatial.Vector3
import mythic.spatial.times
import org.joml.plus

typealias Vertices = List<Vector3>
typealias Edges = List<ImmutableEdge>
typealias Faces = List<ImmutableFace>

class ImmutableEdge(
    val first: Vector3,
    val second: Vector3,
    val faces: MutableList<ImmutableFace>
) {
  val vertices = listOf(first, second)

  val middle: Vector3
    get() = (first + second) * 0.5f

  fun getReference(face: ImmutableFace) = face.edges.first { it.edge == this }

  val references: List<ImmutableEdgeReference>
    get() = faces.map { getReference(it) }

  fun matches(a: Vector3, b: Vector3): Boolean =
      (first == a && second == b)
          || (first == b && second == a)
}

class ImmutableEdgeReference(
    val edge: ImmutableEdge,
    var next: ImmutableEdgeReference?,
    var previous: ImmutableEdgeReference?,
    var direction: Boolean
) {
  val vertices: List<Vector3>
    get() = if (direction) edge.vertices else listOf(edge.second, edge.first)

  val faces: List<ImmutableFace>
    get() = edge.faces

  val first: Vector3
    get() = if (direction) edge.first else edge.second

  val second: Vector3
    get() = if (direction) edge.second else edge.first

  val otherImmutableEdgeReferences: List<ImmutableEdgeReference>
    get() = edge.references.filter { it != this }

  val middle: Vector3
    get() = edge.middle

}

fun getNormal(vertices: Vertices) =
    (vertices[2] - vertices[1]).cross(vertices[0] - vertices[1]).normalize()

private var flexibleFaceDebugCounter = 0L

class ImmutableFace(
    val id: Long,
    var edges: MutableList<ImmutableEdgeReference> = mutableListOf(),
//    var data: Any? = null,
    var normal: Vector3 = Vector3()
) {
  val debugIndex = flexibleFaceDebugCounter++
  val unorderedVertices: List<Vector3>
    get() = edges.map { it.first }

  val vertices: List<Vector3>
    get() = edges.map { it.first }


  fun updateNormal() {
//    if (vertices.size > 2)
    normal = getNormal(unorderedVertices)
  }

  val neighbors: List<ImmutableFace>
    get() = edges.flatMap {
      it.faces
    }
        .filter { it !== this }

  fun edge(first: Vector3, second: Vector3): ImmutableEdgeReference? =
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

typealias ImmutableFaceTable = Map<Long, ImmutableFace>

fun distinctVertices(vertices: Vertices) =
    vertices.distinctBy { System.identityHashCode(it) }

typealias ImmutableEdgeTable = Map<Long, ImmutableEdge>

data class ImmutableMesh(
    val edges: ImmutableEdgeTable = mapOf(),
    val faces: List<ImmutableFace> = listOf()
) {

  val redundantVertices: Vertices
    get() = edges.flatMap { it.value.vertices }

//  val distinctVertices: List<Vector3>
//    get() = redundantVertices.distinct()

  val distinctVertices: Vertices
    get() = distinctVertices(redundantVertices)

//  fun createFace(id: Long): ImmutableFace {
//    val face = ImmutableFace(id)
//    faces.add(face)
//    return face
//  }

  fun createFace(id: Long, vertices: List<Vector3>): ImmutableFace {
    assert(vertices.distinct().size == vertices.size) // Check for duplicate vertices
    val face = ImmutableFace(id)
    replaceFaceVertices(face, vertices)
    return face
  }

  fun createStitchedFace(id: Long, vertices: List<Vector3>): ImmutableFace {
    val face = createFace(id, vertices)
    face.updateNormal()
//    stitchEdges(face.edges)
    return face
  }

  fun getMatchingEdges(first: Vector3, second: Vector3) =
      edges.values.filter { it.matches(first, second) }

  fun createEdge(first: Vector3, second: Vector3, face: ImmutableFace?): ImmutableEdgeReference {
    val faces = if (face == null) mutableListOf() else mutableListOf(face)
    val existingEdges = getMatchingEdges(first, second)
    assert(existingEdges.size < 2)
    if (existingEdges.any()) {
      val edge = existingEdges.first()
      if (face != null)
        edge.faces.add(face)

      return ImmutableEdgeReference(edge, null, null, edge.first == first)
    } else {
      val edge = ImmutableEdge(first, second, faces)
      throw Error("This needs to be handled.")
//      edges.add(edge)
      return ImmutableEdgeReference(edge, null, null, true)
    }

  }

//  fun createEdges(vertices: Vertices) {
//    var previous = vertices.first()
//    var previousEdge: ImmutableEdgeReference? = null
//    for (next in vertices.drop(1)) {
//      val edge = createEdge(previous, next, null)
//      if (previousEdge != null) {
//        edge.previous = previousEdge
//        previousEdge.next = edge
//      }
//      previousEdge = edge
//      previous = next
//    }
//  }

  fun replaceFaceVertices(face: ImmutableFace, initializer: List<Vector3>) {
    var previous = initializer.first()
    var previousEdge: ImmutableEdgeReference? = null
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

//  fun sharedImport(mesh: ImmutableMesh) {
//    faces.addAll(mesh.faces)
//    edges.addAll(mesh.edges)
//  }
//
//  fun sharedImport(meshes: List<ImmutableMesh>) {
//    meshes.forEach { sharedImport(it) }
//  }

}

fun calculateNormals(mesh: ImmutableMesh) {
  for (face in mesh.faces) {
    face.updateNormal()
  }
}
