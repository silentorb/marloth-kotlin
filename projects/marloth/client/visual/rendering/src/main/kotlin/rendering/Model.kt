package rendering

import mythic.sculpting.FlexibleEdge
import mythic.sculpting.FlexibleMesh
import mythic.sculpting.MeshInfo
import mythic.spatial.Vector3

data class MeshElement(
    val mesh: FlexibleMesh,
    val info: MeshInfo,
    val material: Material
)

data class Model(
    val meshes: List<MeshElement>
) {
  val vertices: List<Vector3>
    get() = meshes.flatMap { it.mesh.distinctVertices }

  val edges: List<FlexibleEdge>
    get() = meshes.flatMap { it.mesh.edges }
}
