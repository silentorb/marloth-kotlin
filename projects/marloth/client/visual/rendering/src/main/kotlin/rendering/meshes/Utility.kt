package rendering.meshes

import mythic.glowing.Drawable
import mythic.glowing.SimpleMesh
import mythic.sculpting.FlexibleFace
import mythic.sculpting.FlexibleMesh
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import rendering.*

enum class AttributeName {
  position,
  normal,
  color,
  uv
}

fun createSimpleMesh(faces: List<FlexibleFace>, vertexSchema: VertexSchema, color: Vector4) =
    convertMesh(faces, vertexSchema, temporaryVertexSerializer(color))

fun createSimpleMesh(mesh: FlexibleMesh, vertexSchema: VertexSchema, color: Vector4) =
    convertMesh(mesh, vertexSchema, temporaryVertexSerializer(color))

fun createLineMesh(vertexSchema: VertexSchema) =
    SimpleMesh(vertexSchema, listOf(
        0f, 0f, 0f,
        1f, 0f, 0f
    ))

typealias ModelGenerator = () -> Model

typealias ModelGeneratorMap = Map<MeshType, ModelGenerator>

data class Primitive(
    val mesh: Drawable,
    val material: Material,
    val name: String = ""
)

typealias Primitives = List<Primitive>

typealias MeshMap = Map<MeshType, AdvancedModel>

data class TransientModelElement(
    val faces: List<FlexibleFace>,
    val material: Material
)

fun partitionModelMeshes(model: Model): List<TransientModelElement> {
  if (model.groups.size == 0)
    throw Error("Missing materials")

  return model.groups.map {
    TransientModelElement(it.faces, it.material)
  }
}

fun modelToMeshes(vertexSchemas: VertexSchemas, model: Model): Primitives {
  val sections = partitionModelMeshes(model)
  return sections.map {
    Primitive(createSimpleMesh(it.faces, vertexSchemas.standard, Vector4(1f)), it.material)
  }
}

fun createModelElements(simpleMesh: SimpleMesh<AttributeName>, color: Vector4 = Vector4(1f)) =
    listOf(Primitive(simpleMesh, Material(color)))
