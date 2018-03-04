package mythic.sculpting

import mythic.spatial.Vector3

class HalfEdgeMesh {
  val vertices = ArrayList<HalfEdgeVertex>()
  val edges = ArrayList<HalfEdge>()
  val faces = ArrayList<HalfEdgeFace>()

//  val _face_count: Int
//    get() = faces.count()

//  fun create_vertex(): HalfEdgeVertex {
//    val vertex = HalfEdgeVertex()
//    getVertices.add(vertex)
//    return vertex
//  }

  fun addVertex(vertex: HalfEdgeVertex): HalfEdgeVertex {
    vertices.add(vertex)
    return vertex
  }

  fun addVertex(vector: Vector3) = addVertex(HalfEdgeVertex(vector))

  fun add_vertices(new_vertices: List<HalfEdgeVertex>) {
    vertices.addAll(new_vertices)
  }

  fun add_edge(edge: HalfEdge) {
    edges.add(edge)
  }

//  fun add_face(face: HalfEdgeFace) {
//    faces.add(face)
//  }

  fun add_face(face_vertices: List<HalfEdgeVertex>): HalfEdgeFace {
    add_vertices(face_vertices)
    val face = createFace()
    replaceFaceVertices(face, face_vertices)
    return face
  }

//  fun add_face_from_vectors(face_vertices: List<Vector3>): HalfEdgeFace {
//    add_vertices(face_vertices)
//    return add_face(face_vertices)
//  }

  fun createFace(): HalfEdgeFace {
    val face = HalfEdgeFace(null)
    faces.add(face)
    return face
  }

  fun get_opposite_edge(first: HalfEdgeVertex, second: HalfEdgeVertex): HalfEdge? {
    for (edge in edges) {
      if (edge.vertex === first && edge.next != null && edge.next!!.vertex === second) {
        return edge
      }
    }
    return null
  }

  fun createFace(initializer: List<HalfEdgeVertex>): HalfEdgeFace {
    val face = createFace()
    replaceFaceVertices(face, initializer)
    return face
  }

  fun replaceFaceVertices(face: HalfEdgeFace, initializer: List<HalfEdgeVertex>) {
//    val first = HalfEdge(initializer.first(), null, null, null, face)
//    face.edge = first
//    add_edge(first)
//    var previous = first
//    first.vertex.edge = first
//
//    for (vertex in initializer.drop(1)) {
//      val edge = HalfEdge(vertex, null, null, null, face)
//      add_edge(edge)
//      vertex.edge = edge
//      previous.next = edge
//      edge.previous = previous
////      operations.set_opposite_edge(this, previous)
//      previous = edge
//    }
//
//    previous.next = first
//    first.previous = previous
//    operations.set_opposite_edge(this, previous)
  }

}
