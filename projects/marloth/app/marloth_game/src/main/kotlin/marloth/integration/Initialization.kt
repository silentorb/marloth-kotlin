package marloth.integration

import generation.architecture.MeshInfoMap
import marloth.clienting.Client

fun getMeshInfo(client: Client): MeshInfoMap =
    client.renderer.meshes.filterValues { it.bounds != null }
        .mapValues { it.value.bounds!! }
