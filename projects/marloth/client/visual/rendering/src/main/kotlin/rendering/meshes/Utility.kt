package rendering.meshes

import mythic.glowing.Drawable
import mythic.glowing.SimpleMesh
import mythic.sculpting.ImmutableFace
import mythic.sculpting.ImmutableMesh
import mythic.spatial.*
import rendering.*

enum class AttributeName {
  position,
  normal,
  color,
  uv,
  joints,
  weights
}

fun animatedVertexSerializer(weightMap: WeightMap): ImmutableVertexSerializer {
  return { vertex, face, vertices ->
    vertices.put(vertex)
    vertices.put(0f)
    vertices.put(0f)

    val weights = weightMap[vertex]!!
    vertices.put(weights.first.index.toFloat())
    vertices.put(weights.first.strength)
    vertices.put(weights.second.index.toFloat())
    vertices.put(weights.second.strength)
  }
}

fun simpleVertexSerializer(): ImmutableVertexSerializer {
  return { vertex, face, vertices ->
    vertices.put(vertex)
  }
}

fun createSimpleMesh(faces: List<ImmutableFace>, vertexSchema: VertexSchema) =
    convertMesh(faces, vertexSchema, simpleVertexSerializer())

fun createAnimatedMesh(faces: List<ImmutableFace>, vertexSchema: VertexSchema, weightMap: WeightMap) =
    convertMesh(faces, vertexSchema, animatedVertexSerializer(weightMap))

fun createSimpleMesh(mesh: ImmutableMesh, vertexSchema: VertexSchema) =
    convertMesh(mesh, vertexSchema, simpleVertexSerializer())

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
    val transform: Matrix? = null,
    val name: String = ""
)

typealias Primitives = List<Primitive>

typealias ModelMap = Map<MeshType, AdvancedModel>

data class TransientModelElement(
    val faces: List<ImmutableFace>,
    val material: Material
)

fun partitionModelMeshes(model: Model): List<TransientModelElement> {
  if (model.groups.size == 0)
    throw Error("Missing materials")

  return model.groups.map {
    TransientModelElement(it.faces.toList(), it.material)
  }
}

fun modelToMeshes(vertexSchemas: VertexSchemas, model: Model): Primitives {
  val sections = partitionModelMeshes(model)
  return sections.map {
    Primitive(createSimpleMesh(it.faces, vertexSchemas.shaded), it.material)
  }
}

fun modelToMeshes(vertexSchemas: VertexSchemas, model: Model, weightMap: WeightMap): Primitives {
  val sections = partitionModelMeshes(model)
  return sections.map {
    Primitive(createAnimatedMesh(it.faces, vertexSchemas.animated, weightMap), it.material)
  }
}

fun createModelElements(simpleMesh: SimpleMesh<AttributeName>, color: Vector4 = Vector4(1f)) =
    listOf(Primitive(simpleMesh, Material(color)))
