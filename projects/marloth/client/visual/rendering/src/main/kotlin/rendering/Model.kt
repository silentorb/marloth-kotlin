package rendering

import mythic.sculpting.FlexibleEdge
import mythic.sculpting.FlexibleFace
import mythic.sculpting.FlexibleMesh
import mythic.sculpting.MeshInfo
import mythic.spatial.Vector3

data class MaterialMap(
    val material: Material,
    val faceGroupIndex: Int
)

data class MeshElement(
    val mesh: FlexibleMesh,
    val info: MeshInfo,
    val material: Material
)

data class Model(
    val mesh: FlexibleMesh,
    val info: MeshInfo,
    val materialMaps: List<MaterialMap> = listOf(),
    val defaultMaterial: Material? = null
) {
  val vertices: List<Vector3> get() = mesh.distinctVertices
  val edges: List<FlexibleEdge> get() = mesh.edges
}
