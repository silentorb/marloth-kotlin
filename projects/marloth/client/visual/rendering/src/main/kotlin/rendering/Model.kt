package rendering

import mythic.sculpting.FlexibleEdge
import mythic.sculpting.FlexibleFace
import mythic.sculpting.FlexibleMesh
import mythic.spatial.Vector3
import rendering.meshes.FaceGroup
import rendering.meshes.MeshInfo

data class MaterialMap(
    val material: Material,
    val faceGroup: FaceGroup
)

fun createMaterialMap(material: Material, mesh: FlexibleMesh): MaterialMap {
  return MaterialMap(material, mesh.faces)
}

data class Model(
    val mesh: FlexibleMesh,
    val materials: List<MaterialMap>,
    val info: MeshInfo = MeshInfo()
) {
  val vertices: List<Vector3> get() = mesh.distinctVertices
  val edges: List<FlexibleEdge> get() = mesh.edges
}
