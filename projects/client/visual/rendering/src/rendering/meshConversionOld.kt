package rendering

import sculpting.HalfEdgeMesh

//data class Raw_Mesh(
//    val vertexSize: Int,
//    val vertexBuffer: ByteBuffer,
//
//    val offsetSize: Int,
//    val offsetBuffer: ByteBuffer,
//
//    val countSize: Int,
//    val countBuffer: ByteBuffer
//)
//
//fun convertMeshOld(mesh: HalfEdgeMesh, vertex_schema: VertexSchema): ByteBuffer {
//  val vertex_count = sculpting.query.vertex_count(mesh)
//  val headerSize = 2 * 4
//  val vertexSize = vertex_count * vertex_schema.vertex_size * 4
//  val faceBufferSize = mesh.faces.size * 4
//  val bufferSize = headerSize + vertexSize + faceBufferSize * 2
//
//  val offsetStart = headerSize + vertexSize
////  val countStart = offsetStart + faceBufferSize
//  val buffer = ByteBuffer.allocateDirect(bufferSize)
//  buffer.putInt(vertex_count * vertex_schema.vertex_size)
//  buffer.putInt(mesh.faces.size)
//
////  val offset_buffer = ByteBuffer.allocateDirect()
////  val count_buffer = ByteBuffer.allocateDirect(mesh.faces.size)
//
//  val offset_buffer = buffer.duplicate()
//  offset_buffer.position(offsetStart)
//  val count_buffer = buffer.duplicate()
//  count_buffer.position(offsetStart)
//
//  var offset = 0
//
//  for (polygon in mesh.faces) {
//    sculpting.query.each_edge(polygon, { edge ->
//      val vertex = edge.vertex!!
//
//      // Position
//      buffer.putFloat(vertex.position.x)
//      buffer.putFloat(vertex.position.y)
//      buffer.putFloat(vertex.position.z)
//
//      // Temporary normal code
//      buffer.putFloat(vertex.position.x)
//      buffer.putFloat(vertex.position.y)
//      buffer.putFloat(vertex.position.z)
//
//      // Temporary color code
//      buffer.putFloat(0.5f)
//      buffer.putFloat(0.5f)
//      buffer.putFloat(0f)
//      buffer.putFloat(1f)
//    })
//
//    val face_vertex_count = sculpting.query.vertex_count(polygon)
//    offset_buffer.putInt(offset)
//    offset += face_vertex_count
//    count_buffer.putInt(vertex_count)
//  }
//
//  return buffer
//
////  return Raw_Mesh(
////      vertex_count * vertex_schema.vertex_size,
////      buffer,
////      mesh.faces.size,
////      offset_buffer,
////      mesh.faces.size,
////      count_buffer
////  )
//}
