package rendering

import glowing.SimpleMesh
import glowing.VertexSchema
import org.lwjgl.BufferUtils
import sculpting.HalfEdgeMesh
import spatial.Vector4
import spatial.put

fun convertMesh(mesh: HalfEdgeMesh, vertexSchema: VertexSchema): SimpleMesh {
  val vertex_count = sculpting.query.vertex_count(mesh)
  val vertices = BufferUtils.createFloatBuffer(vertex_count * vertexSchema.size)
  val offsets = BufferUtils.createIntBuffer(mesh.faces.size)
  val counts = BufferUtils.createIntBuffer(mesh.faces.size)
  var i = 0
  var offset = 0

  for (polygon in mesh.faces) {
    sculpting.query.each_edge(polygon, { edge ->
      val vertex = edge.vertex!!

      // Position
      vertices.put(vertex.position)

      // Temporary normal code
      vertices.put(vertex.position)

      // Temporary color code
      vertices.put(Vector4(0.5f, 0.5f, 0f, 1f))
    })

    val face_vertex_count = sculpting.query.vertex_count(polygon)
    offsets.put(offset)
    offset += face_vertex_count
    counts.put(vertex_count)
  }

  vertices.flip()
  val result = SimpleMesh(vertexSchema, vertices, offsets, counts)

//  result.setIndices(indices, 0, indices.size)
  return result
}