package rendering.meshes

import mythic.glowing.SimpleMesh
import mythic.glowing.VertexSchema
import mythic.sculpting.FlexibleMesh
import mythic.sculpting.FlexibleFace
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import mythic.spatial.put
import org.lwjgl.BufferUtils
import java.nio.FloatBuffer

typealias FlexibleVertexSerializer = (vertex: Vector3, face: FlexibleFace, vertices: FloatBuffer) -> Unit

fun temporaryVertexSerializer(color: Vector4): FlexibleVertexSerializer {
  return { vertex, face, vertices ->
    vertices.put(vertex)

    // Temporary color code
    vertices.put(color)
  }
}

fun <T>convertMesh(faces: List<FlexibleFace>, vertexSchema: VertexSchema<T>,
                vertexSerializer: FlexibleVertexSerializer): SimpleMesh<T> {
  val vertex_count = faces.flatMap { it.vertices }.size
  val vertices = BufferUtils.createFloatBuffer(vertex_count * vertexSchema.floatSize)
  val offsets = BufferUtils.createIntBuffer(faces.size)
  val counts = BufferUtils.createIntBuffer(faces.size)
  var offset = 0

  for (polygon in faces) {
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

fun <T>convertMesh(mesh: FlexibleMesh, vertexSchema: VertexSchema<T>, vertexSerializer: FlexibleVertexSerializer) =
    convertMesh(mesh.faces, vertexSchema, vertexSerializer)
