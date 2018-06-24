package rendering

import mythic.breeze.Armature
import mythic.sculpting.FlexibleEdge
import mythic.sculpting.FlexibleMesh
import mythic.spatial.Vector3
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
    val groups: List<MeshGroup>,
    val info: MeshInfo = MeshInfo()
) {
  val vertices: List<Vector3> get() = mesh.distinctVertices
  val edges: List<FlexibleEdge> get() = mesh.edges
}

data class AdvancedModel(
    val primitives: Primitives,
    val armature: Armature? = null
)
