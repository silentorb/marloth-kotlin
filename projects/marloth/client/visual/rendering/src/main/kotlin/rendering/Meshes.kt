package rendering

import mythic.drawing.DrawingVertexSchemas
import mythic.drawing.createDrawingVertexSchemas
import mythic.glowing.SimpleMesh
import mythic.glowing.VertexAttribute
import mythic.glowing.VertexSchema as GenericVertexSchema
import mythic.sculpting.*
import mythic.spatial.*

import mythic.glowing.Drawable
import rendering.meshes.createCartoonHuman
import rendering.meshes.createHuman
import rendering.meshes.createWallLamp

val createCube = {
  val mesh = FlexibleMesh()
//  create.squareDown(mesh, Vector2(1f, 1f), 1f)
  createCube(mesh, Vector3(1f, 1f, 1f))
  Model(
      mesh = mesh,
      materials = listOf(mapMaterial(Material(Vector4(.5f, .5f, .5f, 1f)), mesh))
  )
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

enum class AttributeName {
  position,
  normal,
  color,
  uv
}

typealias VertexSchema = GenericVertexSchema<AttributeName>

data class VertexSchemas(
    val standard: VertexSchema,
    val imported: VertexSchema,
    val textured: VertexSchema,
    val flat: VertexSchema,
    val drawing: DrawingVertexSchemas
)

fun createVertexSchemas() = VertexSchemas(
    standard = VertexSchema(listOf(
        VertexAttribute(AttributeName.position, 3),
        VertexAttribute(AttributeName.normal, 3),
        VertexAttribute(AttributeName.color, 4)
    )),
    textured = VertexSchema(listOf(
        VertexAttribute(AttributeName.position, 3),
        VertexAttribute(AttributeName.normal, 3),
        VertexAttribute(AttributeName.uv, 2)
    )),
    flat = VertexSchema(listOf(
        VertexAttribute(AttributeName.position, 3)
    )),
    imported = VertexSchema(listOf(
        VertexAttribute(AttributeName.position, 3),
        VertexAttribute(AttributeName.normal, 3)
    )),
    drawing = createDrawingVertexSchemas()
)

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
  bear,
  cylinder,
  girl,
  human,
  line,
  monster,
  sphere,
  cube,
  wallLamp,
}

typealias ModelGenerator = () -> Model

typealias ModelGeneratorMap = Map<MeshType, ModelGenerator>

data class ModelElement(
    val mesh: Drawable,
    val material: Material
)

typealias ModelElements = List<ModelElement>
typealias MeshMap = Map<MeshType, ModelElements>

data class TransientModelElement(
    val faces: List<FlexibleFace>,
    val material: Material
)

fun partitionModelMeshes(model: Model): List<TransientModelElement> {
  if (model.materials.size == 0)
    throw Error("Missing materials")

  return model.materials.map {
    TransientModelElement(it.faceGroup, it.material)
  }
}

fun modelToMeshes(vertexSchemas: VertexSchemas, model: Model): ModelElements {
  val sections = partitionModelMeshes(model)
  return sections.map {
    ModelElement(createSimpleMesh(it.faces, vertexSchemas.standard, Vector4(1f)), it.material)
  }
}

fun standardMeshes(): ModelGeneratorMap = mapOf(
    MeshType.cube to createCube,
    MeshType.bear to createCartoonHuman,
    MeshType.human to createHuman,
    MeshType.monster to createCartoonHuman,
    MeshType.wallLamp to createWallLamp
)

//fun importedMeshes(vertexSchemas: VertexSchemas) = mapOf(
//    MeshType.wallLamp to "lamp",
//    MeshType.cube to "cube"
////    MeshType.girl to "girl2/child"
////    MeshType.girl to "child/child"
//)
//    .mapValues { loadGltf(vertexSchemas, "models/" + it.value) }

fun createModelElements(simpleMesh: SimpleMesh<AttributeName>, color: Vector4 = Vector4(1f)) =
    listOf(ModelElement(simpleMesh, Material(color)))

fun createMeshes(vertexSchemas: VertexSchemas): MeshMap = mapOf(
    MeshType.line to createLineMesh(vertexSchemas.flat),
    MeshType.cylinder to createSimpleMesh(createCylinder(), vertexSchemas.standard, Vector4(0.3f, 0.25f, 0.0f, 1f))
)
    .mapValues { createModelElements(it.value) }
    .plus(mapOf(
        MeshType.sphere to createModelElements(createSimpleMesh(createSphere(), vertexSchemas.standard, Vector4(1f)),
            Vector4(0.4f, 0.1f, 0.1f, 1f))
    ))
    .plus(standardMeshes().mapValues {
      modelToMeshes(vertexSchemas, it.value())
    })
//    .plus(importedMeshes(vertexSchemas))