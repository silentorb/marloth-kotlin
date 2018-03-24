package rendering

import mythic.drawing.DrawingVertexSchemas
import mythic.drawing.createDrawingVertexSchemas
import mythic.glowing.SimpleMesh
import mythic.glowing.VertexAttribute
import mythic.glowing.VertexSchema
import mythic.sculpting.*
import mythic.spatial.*
import rendering.meshes.createHuman
import rendering.meshes.createMonster


data class NewMesh(val mesh: HalfEdgeMesh, val vertexSchema: VertexSchema)
typealias NewMeshMap = Map<String, NewMesh>

fun createCube(): FlexibleMesh {
  val mesh = FlexibleMesh()
//  create.squareDown(mesh, Vector2(1f, 1f), 1f)
  createCube(mesh, Vector3(1f, 1f, 1f))
  return mesh
}

fun createCylinder(): FlexibleMesh {
  val mesh = FlexibleMesh()
  createCylinder(mesh, 0.5f, 8, 1f)
  return mesh
}

fun createSphere(): FlexibleMesh {
  val mesh = FlexibleMesh()
  createSphere(mesh, 0.3f, 8, 6)
  return mesh
}

data class VertexSchemas(
    val standard: VertexSchema,
    val textured: VertexSchema,
    val flat: VertexSchema,
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
    flat = VertexSchema(listOf(
        VertexAttribute(0, "position", 3)
    )),
    drawing = createDrawingVertexSchemas()
)

fun createSimpleMeshOld(mesh: HalfEdgeMesh, vertexSchema: VertexSchema) =
    convertMesh(mesh, vertexSchema, temporaryVertexSerializerOld)

fun createSimpleMesh(mesh: FlexibleMesh, vertexSchema: VertexSchema, color: Vector4) =
    convertMesh(mesh, vertexSchema, temporaryVertexSerializer(color))

fun createLineMesh(vertexSchema: VertexSchema) =
    SimpleMesh(vertexSchema, listOf(
        0f, 0f, 0f,
        1f, 0f, 0f
    ))

enum class MeshType {
  character,
  cylinder,
  line,
  monster,
  sphere
}

typealias MeshGenerator = () -> FlexibleMesh

typealias MeshGeneratorMap = Map<MeshType, MeshGenerator>

data class ModelElement(
    val mesh: SimpleMesh,
    val material: Material,
    val offset: Vector3
)

typealias ModelElements =List<ModelElement>
typealias MeshMap = Map<MeshType, ModelElements>

//data class StandardMeshDefinition(
//    val mesh: FlexibleMesh,
//    val color: Vector4
//)

fun standardMeshes() = mapOf(
    MeshType.character to createHuman(),
    MeshType.monster to createMonster()
)

fun createModelElements(simpleMesh: SimpleMesh) =
    listOf(ModelElement(simpleMesh, Material(Vector4(1f)), Vector3()))

fun createMeshes(vertexSchemas: VertexSchemas): MeshMap = mapOf(
    MeshType.line to createLineMesh(vertexSchemas.flat),
    MeshType.cylinder to createSimpleMesh(createCylinder(), vertexSchemas.standard, Vector4(0.3f, 0.25f, 0.0f, 1f)),
    MeshType.sphere to createSimpleMesh(createSphere(), vertexSchemas.standard, Vector4(0.4f, 0.1f, 0.1f, 1f))
)
    .mapValues { createModelElements(it.value) }
    .plus(standardMeshes().map {
      Pair(it.key, it.value.meshes.map {
        ModelElement(createSimpleMesh(it.mesh, vertexSchemas.standard, Vector4(1f)), it.material, Vector3())
      })
    })