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
}

class EdgeReference(
    val edge: FlexibleEdge,
    var next: EdgeReference?,
    var previous: EdgeReference?,
    val direction: Boolean
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
    val edges: MutableList<EdgeReference> = mutableListOf(),
    var data: Any? = null
) {
  val unorderedVertices: List<Vector3>
    get() = edges.flatMap { it.vertices }.distinct()

  val vertices: List<Vector3>
    get() = edges.map { it.first }

  var normal = Vector3()

  fun updateNormal() {
    normal = getNormal(unorderedVertices)
  }

  val neighbors: List<FlexibleFace>
    get() = edges.flatMap {
      it.faces
    }
        .filter { it !== this }
}

class FlexibleMesh {
  //  val vertices: MutableList<Vector3> = mutableListOf()
  val edges: MutableList<FlexibleEdge> = mutableListOf()
  val faces: MutableList<FlexibleFace> = mutableListOf()

  val redundantVertices: List<Vector3>
    get() = edges.flatMap { it.vertices }

//  val distinctVertices: List<Vector3>
//    get() = redundantVertices.distinct()

  val distinctVertices: List<Vector3>
    get() = redundantVertices.distinctBy { System.identityHashCode(it) }

  fun createFace(): FlexibleFace {
    val face = FlexibleFace()
    faces.add(face)
    return face
  }

  fun createFace(vertices: List<Vector3>): FlexibleFace {
    val face = createFace()
    replaceFaceVertices(face, vertices)
    return face
  }

  fun createStitchedFace(vertices: List<Vector3>): FlexibleFace {
    val face = createFace(vertices)
//    stitchEdges(face.edges)
    return face
  }

  fun getMatchingEdges(first: Vector3, second: Vector3) =
      edges.filter { existing ->
        (existing.first == first && existing.second == second)
            || (existing.first == second && existing.second == first)
      }

  fun createEdge(first: Vector3, second: Vector3, face: FlexibleFace?): EdgeReference {
    val faces = if (face == null) mutableListOf() else mutableListOf(face)
    val existingEdges = getMatchingEdges(first, second)
    assert(existingEdges.size < 2)
    val edge = if (existingEdges.any()) {
      val e = existingEdges.first()
      if (face != null)
        e.faces.add(face)
      e
    } else {
      val e = FlexibleEdge(first, second, faces)
      edges.add(e)
      e
    }

    return EdgeReference(edge, null, null, true)
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
