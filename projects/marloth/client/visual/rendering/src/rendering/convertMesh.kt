package rendering

import mythic.glowing.SimpleMesh
import mythic.glowing.VertexSchema
import mythic.sculpting.FlexibleMesh
import mythic.sculpting.FlexibleFace
import mythic.sculpting.HalfEdgeFace
import mythic.sculpting.HalfEdgeMesh
import mythic.sculpting.HalfEdgeVertex
import mythic.sculpting.query.each_edge
import mythic.sculpting.query.getVertexCount
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import mythic.spatial.put
import org.lwjgl.BufferUtils
import java.nio.FloatBuffer

typealias FlexibleVertexSerializer = (vertex: Vector3, face: FlexibleFace, vertices: FloatBuffer) -> Unit
typealias HalfEdgeVertexSerializer = (vertex: HalfEdgeVertex, face: HalfEdgeFace, vertices: FloatBuffer) -> Unit

val temporaryVertexSerializerOld: HalfEdgeVertexSerializer = { vertex, face, vertices ->
  vertices.put(vertex.position)

  // Temporary color code
  vertices.put(Vector4(0.5f, 0.5f, 0f, 1f))
}

fun temporaryVertexSerializer(color: Vector4): FlexibleVertexSerializer {
  return { vertex, face, vertices ->
    vertices.put(vertex)

    // Temporary color code
    vertices.put(color)
  }
}

fun convertMesh(mesh: HalfEdgeMesh, vertexSchema: VertexSchema, vertexSerializer: HalfEdgeVertexSerializer): SimpleMesh {
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

fun convertMesh(mesh: FlexibleMesh, vertexSchema: VertexSchema,
                vertexSerializer: FlexibleVertexSerializer): SimpleMesh {
  val vertex_count = mesh.vertices.size
//  val v2 = mesh.faces.flatMap { it.vertices }
  val vertices = BufferUtils.createFloatBuffer(vertex_count * vertexSchema.floatSize)
  val offsets = BufferUtils.createIntBuffer(mesh.faces.size)
  val counts = BufferUtils.createIntBuffer(mesh.faces.size)
  var offset = 0

  for (polygon in mesh.faces) {
    polygon.vertices.forEach { v ->
      vertices.put(v)
      vertexSerializer(v, polygon, vertices)
    }

    val face_vertex_count = polygon.vertices.size
    offsets.put(offset)
    counts.put(face_vertex_count)
    offset += face_vertex_count
  }

  vertices.flip()
  offsets.flip()
  counts.flip()
  return SimpleMesh(vertexSchema, vertices, offsets, counts)
}