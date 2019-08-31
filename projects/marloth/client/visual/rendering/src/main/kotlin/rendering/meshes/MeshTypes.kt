package rendering.meshes

import mythic.sculpting.EdgeReference
import mythic.sculpting.ImmutableMesh
import mythic.sculpting.ImmutableFace
import mythic.spatial.Vector3

typealias EdgeGroup = Map<EdgeReference, Float>
typealias VertexGroup = Map<Vector3, Float>

data class MeshInfo(
    val vertexGroups: List<VertexGroup> = listOf(),
    val edgeGroups: List<EdgeGroup> = listOf()
)
