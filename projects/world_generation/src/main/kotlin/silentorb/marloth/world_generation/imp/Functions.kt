package silentorb.marloth.world_generation.imp

import silentorb.imp.core.Key
import silentorb.marloth.world_generation.GetSpatialNode
import silentorb.mythic.spatial.Matrix

fun transformSpatialNode(arguments: Map<Key, Any>, operation: (Matrix) -> Matrix): GetSpatialNode {
  val source = arguments["source"] as GetSpatialNode
  return { input ->
    val node = source(input)
    node.copy(
        transform = operation(node.transform)
    )
  }
}
