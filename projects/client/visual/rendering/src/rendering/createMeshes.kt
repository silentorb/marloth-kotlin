package rendering

import glowing.SimpleMesh
import glowing.VertexAttribute
import glowing.VertexSchema
import scenery.Depiction
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

fun createMeshes(): MeshMap {
  val vertexSchema = VertexSchema(listOf(
      VertexAttribute(0, "position", 3),
      VertexAttribute(1, "normal", 3),
      VertexAttribute(2, "color", 4)
  ))
  val newMeshes = createMeshes(vertexSchema)
  return newMeshes.mapValues({ (_, m) -> convertMesh(m.mesh, m.vertexSchema) })
}

fun createMeshes(vertexSchema: VertexSchema): NewMeshMap = mapOf(
    "child" to NewMesh(createCube(), vertexSchema),
    "test" to NewMesh(sculpting.create.flatTest(), vertexSchema)
)

