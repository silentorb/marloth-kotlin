package rendering

import mythic.glowing.SimpleMesh
import mythic.glowing.VertexSchema
import org.lwjgl.BufferUtils
import mythic.sculpting.HalfEdgeMesh
import mythic.sculpting.query
import mythic.spatial.Vector4
import mythic.spatial.put

fun convertMesh(mesh: HalfEdgeMesh, vertexSchema: VertexSchema, color: Vector4 = Vector4(0.5f, 0.5f, 0f, 1f)): SimpleMesh {
  val vertex_count = query.vertex_count(mesh)
  val vertices = BufferUtils.createFloatBuffer(vertex_count * vertexSchema.floatSize)
  val offsets = BufferUtils.createIntBuffer(mesh.faces.size)
  val counts = BufferUtils.createIntBuffer(mesh.faces.size)
  var offset = 0

  for (polygon in mesh.faces) {
    query.each_edge(polygon, { edge ->
      val vertex = edge.vertex

      // Position
      vertices.put(vertex.position)

      // Temporary normal code
      vertices.put(vertex.position)

      // Temporary color code
      vertices.put(color)
    })

    val face_vertex_count = query.vertex_count(polygon)
    offsets.put(offset)
    counts.put(face_vertex_count)
    offset += face_vertex_count
  }


  vertices.flip()
  offsets.flip()
  counts.flip()
  val result = SimpleMesh(vertexSchema, vertices, offsets, counts)

//  result.setIndices(indices, 0, indices.size)
  return result
}