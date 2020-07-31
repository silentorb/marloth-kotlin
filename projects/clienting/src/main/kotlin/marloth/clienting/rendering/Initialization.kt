package marloth.clienting.rendering

import marloth.scenery.enums.MeshShapeMap
import marloth.clienting.Client

fun getMeshInfo(client: Client): MeshShapeMap =
    client.renderer.meshes
        .filterValues { it.bounds != null }
        .mapValues { it.value.bounds!! }
        .plus(
            client.meshLoadingState.remaining
                .filter { it.model.collision != null }
                .associate { it.name to it.model.collision!! }
        )

