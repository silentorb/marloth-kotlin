package sculpting

import spatial.*

class create {
  companion object {
    fun squareDown(mesh: HalfEdgeMesh, size: Vector2, z: Float): Face {
      val half = size * 0.5f;
      return mesh.add_face(listOf(
      Vertex( Vector3( -half.x, -half.y, z ) ),
      Vertex( Vector3( -half.x, half.y, z ) ),
      Vertex( Vector3( half.x, half.y, z ) ),
      Vertex( Vector3( half.x, -half.y, z ) )
      ))
    }

//    squareUp(mesh: HalfEdgeMesh, size: Vector2, z: Float): Face {
//      val half = size * 0.5f;
//      return mesh.add_face(listOf(
//      Vertex ( Vector3( -half.x, -half.y, z ) ),
//      Vertex ( Vector3( half.x, -half.y, z ) ),
//      Vertex ( Vector3( half.x, half.y, z ) ),
//      Vertex ( Vector3( -half.x, half.y, z ) )
//      ))
//    }
  }
}