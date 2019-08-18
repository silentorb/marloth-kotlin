package rendering

import mythic.sculpting.ImmutableEdge
import mythic.sculpting.ImmutableFace
import mythic.sculpting.ImmutableMesh
import mythic.spatial.Vector3
import rendering.meshes.MeshInfo
import rendering.meshes.Primitives
import rendering.texturing.FaceTextureMap
import scenery.ArmatureId
import scenery.Light
import scenery.MeshName
import scenery.Shape

data class MeshGroup(
    val material: Material,
    val faces: Collection<ImmutableFace>,
    val name: String = "Unnamed"
)

fun mapMaterialToMesh(material: Material, mesh: ImmutableMesh): MeshGroup {
  return MeshGroup(material, mesh.faces.values)
}

fun mapMaterialToManyMeshes(material: Material, meshes: List<ImmutableMesh>): MeshGroup {
  return MeshGroup(material, meshes.flatMap { it.faces.values })
}

data class Model(
    val mesh: ImmutableMesh,
    val groups: List<MeshGroup> = listOf(),
    val info: MeshInfo = MeshInfo(),
    val textureMap: FaceTextureMap? = null
) {
  val vertices: List<Vector3> get() = mesh.distinctVertices
  val edges: List<ImmutableEdge> get() = mesh.edges.values.toList()
}

data class VertexWeight(
    val index: Int,
    val strength: Float
)

typealias VertexWeights = Pair<VertexWeight, VertexWeight>

typealias WeightMap = Map<Vector3, VertexWeights>

data class AdvancedModel(
    val primitives: Primitives,
    val model: Model? = null,
    val armature: Armature? = null
)

data class ModelMesh(
    val id: MeshName,
    val primitives: Primitives,
    val lights: List<Light> = listOf(),
    val bounds: Shape? = null
)

typealias ModelMeshMap = Map<MeshName, ModelMesh>

fun getArmatureId(name: String): ArmatureId {
  val formattedName = toCamelCase(name)
  val id = ArmatureId.values().firstOrNull { it.name == formattedName }
  if (id == null)
    throw Error("Could not find ArmatureId " + formattedName)

  return id
}
