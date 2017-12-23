package sculpting

class query {
  companion object {

    fun vertex_count(face: Face): Int {
      var result = 1
      var edge = face.edge!!.next

      while (edge !== face.edge) {
        edge = edge!!.next
        ++result
      }

      return result
    }

    fun vertex_count(mesh: HalfEdgeMesh): Int {
      var result = 0
      for (polygon in mesh.faces) {
        result += vertex_count(polygon)
      }
      return result
    }

    inline fun <T>each_edge(face: Face, action: (Edge) -> T) {
      var edge = face.edge!!
      do {
        action(edge)
        edge = edge.next!!
      } while (edge !== face.edge)
    }

    inline fun <T>each_vertex(face: Face, action: (Vertex) -> T) {
      each_edge(face, { action(it.vertex!!) })
    }

    fun vertices(face: Face): List<Vertex>{
      val result = ArrayList<Vertex>()
      each_vertex(face, { result.add(it) })
      return result
    }

    fun edges(face: Face): List<Edge>{
      val result = ArrayList<Edge>()
      each_edge(face, { result.add(it) })
      return result
    }
  }
}