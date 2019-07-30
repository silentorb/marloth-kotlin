package marloth.integration

import marloth.clienting.Client
import simulation.misc.MeshInfoMap

fun getMeshInfo(client: Client): MeshInfoMap =
    client.renderer.meshes.filterValues { it.bounds != null }
        .mapValues { it.value.bounds!! }
