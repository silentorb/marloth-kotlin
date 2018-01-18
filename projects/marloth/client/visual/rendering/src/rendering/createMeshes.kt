package rendering

import mythic.drawing.DrawingVertexSchemas
import mythic.drawing.createDrawingVertexSchemas
import mythic.glowing.SimpleMesh
import mythic.glowing.VertexAttribute
import mythic.glowing.VertexSchema
import mythic.sculpting.HalfEdgeMesh
import mythic.sculpting.create
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import mythic.spatial.put

typealias MeshMap = Map<String, SimpleMesh>

data class NewMesh(val mesh: HalfEdgeMesh, val vertexSchema: VertexSchema)
typealias NewMeshMap = Map<String, NewMesh>

fun createCube(): HalfEdgeMesh {
  val mesh = HalfEdgeMesh()
//  create.squareDown(mesh, Vector2(1f, 1f), 1f)
  create.cube(mesh, Vector3(1f, 1f, 1f))
  return mesh
}

data class VertexSchemas(
    val standard: VertexSchema,
    val textured: VertexSchema,
    val drawing: DrawingVertexSchemas
)

fun createVertexSchemas() = VertexSchemas(
    standard = VertexSchema(listOf(
        VertexAttribute(0, "position", 3),
        VertexAttribute(1, "normal", 3),
        VertexAttribute(2, "color", 4)
    )),
    textured = VertexSchema(listOf(
        VertexAttribute(0, "position", 3),
        VertexAttribute(1, "normal", 3),
        VertexAttribute(2, "uv", 2)
    )),
    drawing = createDrawingVertexSchemas()
)

fun createMeshes(vertexSchemas: VertexSchemas): MeshMap {
  val newMeshes = createMeshes(vertexSchemas.standard)
  return newMeshes.mapValues({ (_, m) -> convertMesh(m.mesh, m.vertexSchema, temporaryVertexSerializer) })
}

fun createMeshes(vertexSchema: VertexSchema): NewMeshMap = mapOf(
    "child" to NewMesh(createCube(), vertexSchema),
    "test" to NewMesh(create.flatTest(), vertexSchema)
)

