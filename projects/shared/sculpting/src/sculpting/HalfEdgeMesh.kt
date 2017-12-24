package sculpting

class HalfEdgeMesh {
  val vertices = ArrayList<Vertex>()
  val edges = ArrayList<Edge>()
  val faces = ArrayList<Face>()

//  val _face_count: Int
//    get() = faces.count()

//  fun create_vertex(): sculpting.Vertex {
//    val vertex = sculpting.Vertex()
//    vertices.add(vertex)
//    return vertex
//  }

  fun add_vertex(vertex: Vertex): Vertex {
    vertices.add(vertex)
    return vertex
  }

  fun add_vertices(new_vertices: Array<Vertex>) {
    vertices.addAll(new_vertices)
  }

  fun add_edge(edge: Edge) {
    edges.add(edge)
  }

//  fun add_face(face: Face) {
//    faces.add(face)
//  }

  fun add_face(face_vertices: Array<Vertex>): Face {
    add_vertices(face_vertices)
    val face = create_face()
    replace_face_vertices(face, face_vertices)
    return face
  }

//  fun add_face_from_vectors(face_vertices: List<Vector3>): Face {
//    add_vertices(face_vertices)
//    return add_face(face_vertices)
//  }

  fun create_face(): Face {
    val face = Face(null)
    faces.add(face)
    return face
  }

  fun get_opposite_edge(first: Vertex, second: Vertex): Edge? {
    for (edge in edges) {
      if (edge.vertex === first && edge.next.vertex === second) {
        return edge
      }
    }
    return null
  }

  fun replace_face_vertices(face: Face, initializer: Array<Vertex>) {
    val first = Edge(initializer.first(), face)
    face.edge = first
    add_edge(first)
    var previous = first
    first.vertex.edge = first

    for (vertex in initializer.drop(1)) {
      val edge = Edge(vertex, face)
      add_edge(edge)
      vertex.edge = edge
      previous.next = edge
      operations.set_opposite_edge(this, previous)
      previous = edge
    }

    previous.next = first
    operations.set_opposite_edge(this, previous)
  }

}
