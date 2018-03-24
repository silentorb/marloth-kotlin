package rendering.meshes

import mythic.sculpting.FlexibleMesh
import mythic.sculpting.FlexibleEdge
import mythic.sculpting.FlexibleFace
import mythic.spatial.Vector3

typealias EdgeGroup = Map<FlexibleEdge, Float>
typealias VertexGroup = Map<Vector3, Float>
typealias FaceGroup = List<FlexibleFace>

data class MeshInfo(
    val vertexGroups: List<VertexGroup>,
    val edgeGroups: List<EdgeGroup>
)

data class MeshNode<Ports>(
    val mesh: FlexibleMesh,
    val ports: Ports,
    val info: MeshInfo
)
