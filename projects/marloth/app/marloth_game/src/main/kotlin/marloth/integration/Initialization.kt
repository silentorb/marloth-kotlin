package marloth.integration

import generation.misc.MeshShapeMap
import marloth.clienting.Client

fun getMeshInfo(client: Client): MeshShapeMap =
    client.renderer.meshes.filterValues { it.bounds != null }
        .mapValues { it.value.bounds!! }
