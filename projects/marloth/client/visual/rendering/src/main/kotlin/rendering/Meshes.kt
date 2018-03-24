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

fun createSimpleMesh(faces: List<FlexibleFace>, vertexSchema: VertexSchema, color: Vector4) =
    convertMesh(faces, vertexSchema, temporaryVertexSerializer(color))

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

typealias ModelGenerator = () -> Model

typealias ModelGeneratorMap = Map<MeshType, ModelGenerator>

data class ModelElement(
    val mesh: SimpleMesh,
    val material: Material
)

data class TransientModelElement(
    val faces: List<FlexibleFace>,
    val material: Material
)

fun partitionModelMeshes(model: Model): List<TransientModelElement> {
  if (model.materialMaps.size == 0) {
    if (model.defaultMaterial == null)
      throw Error("Incomplete material info")

    return listOf(TransientModelElement(model.mesh.faces, model.defaultMaterial))
  }

  throw Error("Not implemented yet.")
}

fun modelToMeshes(vertexSchemas: VertexSchemas, model: Model): ModelElements {
  val sections = partitionModelMeshes(model)
  return sections.map {
    ModelElement(createSimpleMesh(it.faces, vertexSchemas.standard, Vector4(1f)), it.material)
  }
}

typealias ModelElements = List<ModelElement>
typealias MeshMap = Map<MeshType, ModelElements>

fun standardMeshes(): ModelGeneratorMap = mapOf(
    MeshType.character to createHuman,
    MeshType.monster to createMonster
)

fun createModelElements(simpleMesh: SimpleMesh) =
    listOf(ModelElement(simpleMesh, Material(Vector4(1f))))

fun createMeshes(vertexSchemas: VertexSchemas): MeshMap = mapOf(
    MeshType.line to createLineMesh(vertexSchemas.flat),
    MeshType.cylinder to createSimpleMesh(createCylinder(), vertexSchemas.standard, Vector4(0.3f, 0.25f, 0.0f, 1f)),
    MeshType.sphere to createSimpleMesh(createSphere(), vertexSchemas.standard, Vector4(0.4f, 0.1f, 0.1f, 1f))
)
    .mapValues { createModelElements(it.value) }
    .plus(standardMeshes().mapValues {
      modelToMeshes(vertexSchemas, it.value())
    })