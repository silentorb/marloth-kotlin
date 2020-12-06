package simulation.physics

import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.scenery.getNodeTransform
import silentorb.mythic.physics.Body
import silentorb.mythic.spatial.Quaternion

fun graphToBody(graph: Graph, node: String): Body {
  val transform = getNodeTransform(graph, node)
  return Body(
      position = transform.translation(),
      orientation = Quaternion().fromUnnormalized(transform),
      scale = transform.getScale()
  )
}
