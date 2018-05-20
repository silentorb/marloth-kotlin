package rendering.meshes

import mythic.sculpting.EdgeReference
import mythic.sculpting.FlexibleMesh
import mythic.sculpting.FlexibleFace
import mythic.spatial.Vector3

typealias EdgeGroup = Map<EdgeReference, Float>
typealias VertexGroup = Map<Vector3, Float>
typealias Faces = List<FlexibleFace>

data class MeshInfo(
    val vertexGroups: List<VertexGroup> = listOf(),
    val edgeGroups: List<EdgeGroup> = listOf()
)

data class MeshNode<Ports>(
    val mesh: FlexibleMesh,
    val ports: Ports,
    val info: MeshInfo
)
