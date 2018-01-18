package rendering

import mythic.glowing.SimpleMesh
import mythic.glowing.VertexSchema
import org.lwjgl.BufferUtils
import mythic.sculpting.HalfEdgeMesh
import mythic.sculpting.Vertex
import mythic.sculpting.Face
import mythic.sculpting.query.each_edge
import mythic.sculpting.query.getVertexCount
import mythic.spatial.Vector4
import mythic.spatial.put
import java.nio.FloatBuffer

typealias VertexSerializer = (vertex: Vertex, face: Face, vertices: FloatBuffer) -> Unit

val temporaryVertexSerializer: VertexSerializer = { vertex, face, vertices ->
  vertices.put(vertex.position)

  // Temporary color code
  vertices.put(Vector4(0.5f, 0.5f, 0f, 1f))
}

fun convertMesh(mesh: HalfEdgeMesh, vertexSchema: VertexSchema, vertexSerializer: VertexSerializer): SimpleMesh {
  val vertex_count = getVertexCount(mesh)
  val vertices = BufferUtils.createFloatBuffer(vertex_count * vertexSchema.floatSize)
  val offsets = BufferUtils.createIntBuffer(mesh.faces.size)
  val counts = BufferUtils.createIntBuffer(mesh.faces.size)
  var offset = 0

  for (polygon in mesh.faces) {
    each_edge(polygon, { edge ->
      val vertex = edge.vertex
      vertices.put(vertex.position)
      vertexSerializer(vertex, polygon, vertices)
    })

    val face_vertex_count = getVertexCount(polygon)
    offsets.put(offset)
    counts.put(face_vertex_count)
    offset += face_vertex_count
  }

  vertices.flip()
  offsets.flip()
  counts.flip()
  return SimpleMesh(vertexSchema, vertices, offsets, counts)
}