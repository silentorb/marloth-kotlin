package rendering

import glowing.SimpleMesh
import glowing.VertexSchema
import sculpting.HalfEdgeMesh
import spatial.*

typealias MeshMap = Map<String, SimpleMesh>

data class NewMesh(val mesh: HalfEdgeMesh, val vertexSchema: VertexSchema)
typealias NewMeshMap = Map<String, NewMesh>

fun createCube(): HalfEdgeMesh {
  val mesh = HalfEdgeMesh()
//  sculpting.create.squareDown(mesh, Vector2(1f, 1f), 1f)
  sculpting.create.cube(mesh, Vector3(1f, 1f, 1f))
  return mesh
}

fun createMeshes(vertexSchema: VertexSchema): NewMeshMap = mapOf(
    "cube" to NewMesh(createCube(), vertexSchema)
)