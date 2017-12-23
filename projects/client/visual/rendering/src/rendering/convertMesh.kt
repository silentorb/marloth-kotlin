package rendering

import com.badlogic.gdx.graphics.Mesh
import sculpting.HalfEdgeMesh

fun convertMesh(mesh: HalfEdgeMesh, vertexSchema: VertexSchema): Mesh {
  val vertex_count = sculpting.query.vertex_count(mesh)
  val vertexSize = vertexSchema.sumBy({ it.numComponents })
  val vertices = FloatArray(vertex_count * vertexSize)
//  val indices = ShortArray(vertex_count)

//  var offset = 0
  var i = 0

  for (polygon in mesh.faces) {
    sculpting.query.each_edge(polygon, { edge ->
      val vertex = edge.vertex!!

      // Position
      vertices[i++] = vertex.position.x
      vertices[i++] = vertex.position.y
      vertices[i++] = vertex.position.z

      // Temporary normal code
      vertices[i++] = vertex.position.x
      vertices[i++] = vertex.position.y
      vertices[i++] = vertex.position.z

      // Temporary color code
      vertices[i++] = 0.5f
      vertices[i++] = 0.5f
      vertices[i++] = 0f
      vertices[i++] = 1f
    })

//    val face_vertex_count = sculpting.query.vertex_count(polygon)
//    offset_buffer.putInt(offset)
//    offset += face_vertex_count
//    count_buffer.putInt(vertex_count)
  }

  val result = Mesh(true, vertex_count, 0, *vertexSchema)

  result.setVertices(vertices)
//  result.setIndices(indices, 0, indices.size)
  return result
}