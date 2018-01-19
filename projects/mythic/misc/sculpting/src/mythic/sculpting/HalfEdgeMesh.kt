package mythic.sculpting

import mythic.spatial.Vector3

class HalfEdgeMesh {
  val vertices = ArrayList<Vertex>()
  val edges = ArrayList<Edge>()
  val faces = ArrayList<Face>()

//  val _face_count: Int
//    get() = faces.count()

//  fun create_vertex(): Vertex {
//    val vertex = Vertex()
//    getVertices.add(vertex)
//    return vertex
//  }

  fun addVertex(vertex: Vertex): Vertex {
    vertices.add(vertex)
    return vertex
  }

  fun addVertex(vector: Vector3) = addVertex(Vertex(vector))

  fun add_vertices(new_vertices: List<Vertex>) {
    vertices.addAll(new_vertices)
  }

  fun add_edge(edge: Edge) {
    edges.add(edge)
  }

//  fun add_face(face: Face) {
//    faces.add(face)
//  }

  fun add_face(face_vertices: List<Vertex>): Face {
    add_vertices(face_vertices)
    val face = createFace()
    replaceFaceVertices(face, face_vertices)
    return face
  }

//  fun add_face_from_vectors(face_vertices: List<Vector3>): Face {
//    add_vertices(face_vertices)
//    return add_face(face_vertices)
//  }

  fun createFace(): Face {
    val face = Face(null)
    faces.add(face)
    return face
  }

  fun get_opposite_edge(first: Vertex, second: Vertex): Edge? {
    for (edge in edges) {
      if (edge.vertex === first && edge.next != null && edge.next!!.vertex === second) {
        return edge
      }
    }
    return null
  }

  fun createFace(initializer: List<Vertex>): Face {
    val face = createFace()
    replaceFaceVertices(face, initializer)
    return face
  }

  fun replaceFaceVertices(face: Face, initializer: List<Vertex>) {
    val first = Edge(initializer.first(), null, null, null, face)
    face.edge = first
    add_edge(first)
    var previous = first
    first.vertex.edge = first

    for (vertex in initializer.drop(1)) {
      val edge = Edge(vertex, null, null, null, face)
      add_edge(edge)
      vertex.edge = edge
      previous.next = edge
      edge.previous = previous
      operations.set_opposite_edge(this, previous)
      previous = edge
    }

    previous.next = first
    first.previous = previous
    operations.set_opposite_edge(this, previous)
  }

}
