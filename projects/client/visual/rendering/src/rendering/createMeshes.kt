package rendering

import com.badlogic.gdx.graphics.Mesh
import sculpting.HalfEdgeMesh
import spatial.*

typealias MeshMap = Map<String, Mesh>

data class NewMesh(val mesh: HalfEdgeMesh, val vertexSchema: VertexSchema)
typealias NewMeshMap = Map<String, NewMesh>

fun createCube(): HalfEdgeMesh {
  val mesh = HalfEdgeMesh()
  sculpting.create.squareDown(mesh, Vector2(1f, 1f), 1f)
  return mesh
}

fun createMeshes(vertexSchema: VertexSchema): NewMeshMap = mapOf(
    "cube" to NewMesh(createCube(), vertexSchema)
)