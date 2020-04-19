package generation.architecture.definition

import marloth.scenery.enums.MeshAttribute
import generation.architecture.misc.MeshQuery

data class RandomMeshQuery(
    val query: MeshQuery,
    val nothingChance: Float = 0f
)

typealias ConnectTypeMeshQueryMap = Map<ConnectionType, RandomMeshQuery>

fun connectionTypesToMeshQueries(): ConnectTypeMeshQueryMap = mapOf(
    ConnectionType.decoratedWall to RandomMeshQuery(
        query = MeshQuery(all = setOf(MeshAttribute.wall, MeshAttribute.decorated))
    ),
    ConnectionType.doorway to RandomMeshQuery(
        query = MeshQuery(all = setOf(MeshAttribute.doorway))
    ),
    ConnectionType.plainWall to RandomMeshQuery(
        query = MeshQuery(all = setOf(MeshAttribute.wall, MeshAttribute.plain))
    ),
    ConnectionType.solidWall to RandomMeshQuery(
        query = MeshQuery(all = setOf(MeshAttribute.wall, MeshAttribute.solid))
    )
)
