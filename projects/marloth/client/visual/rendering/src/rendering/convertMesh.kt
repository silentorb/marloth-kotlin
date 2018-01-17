package rendering

import mythic.glowing.SimpleMesh
import mythic.glowing.VertexSchema
import org.lwjgl.BufferUtils
import mythic.sculpting.HalfEdgeMesh
import mythic.sculpting.Vertex
import mythic.sculpting.query
import mythic.spatial.put
import java.nio.FloatBuffer

typealias VertexSerializer = (vertex: Vertex, vertices: FloatBuffer) -> Unit

fun convertMesh(mesh: HalfEdgeMesh, vertexSchema: VertexSchema, vertexSerializer: VertexSerializer): SimpleMesh {
  val vertex_count = query.vertex_count(mesh)
  val vertices = BufferUtils.createFloatBuffer(vertex_count * vertexSchema.floatSize)
  val offsets = BufferUtils.createIntBuffer(mesh.faces.size)
  val counts = BufferUtils.createIntBuffer(mesh.faces.size)
  var offset = 0

  for (polygon in mesh.faces) {
    query.each_edge(polygon, { edge ->
      val vertex = edge.vertex
      vertices.put(vertex.position)
      vertexSerializer(vertex, vertices)
    })

    val face_vertex_count = query.vertex_count(polygon)
    offsets.put(offset)
    counts.put(face_vertex_count)
    offset += face_vertex_count
  }

  vertices.flip()
  offsets.flip()
  counts.flip()
  return SimpleMesh(vertexSchema, vertices, offsets, counts)
}