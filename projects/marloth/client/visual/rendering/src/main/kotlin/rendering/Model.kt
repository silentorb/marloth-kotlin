package rendering

import mythic.sculpting.ImmutableEdge
import mythic.sculpting.ImmutableMesh
import mythic.spatial.Vector3
import rendering.meshes.Faces
import rendering.meshes.MeshInfo
import rendering.meshes.Primitives

data class MeshGroup(
    val material: Material,
    val faces: Faces,
    val name: String = "Unnamed"
)

fun mapMaterialToMesh(material: Material, mesh: ImmutableMesh): MeshGroup {
  return MeshGroup(material, mesh.faces)
}

fun mapMaterialToManyMeshes(material: Material, meshes: List<ImmutableMesh>): MeshGroup {
  return MeshGroup(material, meshes.flatMap { it.faces })
}

data class Model(
    val mesh: ImmutableMesh,
    val groups: List<MeshGroup> = listOf(),
    val info: MeshInfo = MeshInfo(),
    val textureMap: FaceTextureMap? = null
) {
  val vertices: List<Vector3> get() = mesh.distinctVertices
  val edges: List<ImmutableEdge> get() = mesh.edges
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

fun dispose(model: AdvancedModel) {
  model.primitives.forEach { it.mesh.dispose() }
}
