package marloth.clienting.editing

import silentorb.mythic.ent.*
import silentorb.mythic.ent.scenery.Expanders
import silentorb.mythic.ent.scenery.getNodeTransform
import silentorb.mythic.scenery.SceneProperties
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.getCenter
import silentorb.mythic.spatial.getYawAndPitch

object MarlothExpanders {
  const val simpleBridge = "simpleBridge"
}

fun marlothExpanders(): Expanders = mapOf(
    MarlothExpanders.simpleBridge to { library, graph, node ->
      val entries = getProperties(graph, node)
      val connections = getPropertyValues<Key>(entries, node, SceneProperties.connects)
      if (connections.size != 2)
        graph
      else {
        val (a, b) = connections
        val aTransform = getNodeTransform(graph, a)
        val bTransform = getNodeTransform(graph, b)
        val aLocation = aTransform.translation()
        val bLocation = bTransform.translation()
        if (aLocation == bLocation)
          graph
        else {
          val center = getCenter(aLocation, bLocation)
          val distance = aLocation.distance(bLocation)
          val vector = (bLocation - aLocation).normalize()
          val (yaw, pitch) = getYawAndPitch(vector)
          val mesh = getGraphValue<Key>(entries, node, SceneProperties.mesh)
          val shape = library.meshShapes[mesh]
          val offset = if (shape != null)
            Vector3(0f, 0f, -shape.height / 2f)
          else
            Vector3.zero

          val length = if (shape != null)
            shape.x
          else
            1f

          val scale = Vector3((distance / length) + 0.05f, 1f, 1f)

          replaceValues(graph,
              setOf(
                  Entry(node, SceneProperties.translation, center + offset),
                  Entry(node, SceneProperties.rotation, Vector3(0f, -pitch, yaw)),
                  Entry(node, SceneProperties.scale, scale),
              )
          )
        }
      }
    }
)
