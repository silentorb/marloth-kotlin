package generation.architecture.definition

import generation.architecture.misc.MeshQuery

typealias ConnectTypeMeshQueryMap = Map<ConnectionType, MeshQuery>

fun connectionTypesToMeshQueries(): ConnectTypeMeshQueryMap = mapOf(
    ConnectionType.decoratedWall to MeshQuery(all = setOf(MeshAttribute.wall, MeshAttribute.decorated)),
    ConnectionType.doorway to MeshQuery(all = setOf(MeshAttribute.doorway)),
    ConnectionType.plainWall to MeshQuery(all = setOf(MeshAttribute.wall, MeshAttribute.plain)),
    ConnectionType.solidWall to MeshQuery(all = setOf(MeshAttribute.wall, MeshAttribute.solid))
)
