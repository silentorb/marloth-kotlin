package rendering

import mythic.breeze.Armature
import mythic.sculpting.FlexibleEdge
import mythic.sculpting.FlexibleMesh
import mythic.spatial.Vector3m
import rendering.meshes.Faces
import rendering.meshes.MeshInfo
import rendering.meshes.Primitives

data class MeshGroup(
    val material: Material,
    val faces: Faces,
    val name: String = "Unnamed"
)

fun mapMaterialToMesh(material: Material, mesh: FlexibleMesh): MeshGroup {
  return MeshGroup(material, mesh.faces)
}

fun mapMaterialToManyMeshes(material: Material, meshes: List<FlexibleMesh>): MeshGroup {
  return MeshGroup(material, meshes.flatMap { it.faces })
}

data class Model(
    val mesh: FlexibleMesh,
    val groups: List<MeshGroup> = listOf(),
    val info: MeshInfo = MeshInfo(),
    val textureMap: FaceTextureMap? = null
) {
  val vertices: List<Vector3m> get() = mesh.distinctVertices
  val edges: List<FlexibleEdge> get() = mesh.edges
}

data class VertexWeight(
    val index: Int,
    val strength: Float
)

typealias VertexWeights = Pair<VertexWeight, VertexWeight>

typealias WeightMap = Map<Vector3m, VertexWeights>

data class AdvancedModel(
    val primitives: Primitives,
    val model: Model? = null,
    val armature: Armature? = null,
    val weights: WeightMap = mapOf()
)

fun dispose(model: AdvancedModel) {
  model.primitives.forEach { it.mesh.dispose() }
}
