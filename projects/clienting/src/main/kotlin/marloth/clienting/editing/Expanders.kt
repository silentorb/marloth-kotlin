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
        val (a1, a2) = a.split("+")
        val (b1, b2) = b.split("+")
        val a1Location = getNodeTransform(graph, a1).translation()
        val a2Location = getNodeTransform(graph, a2).translation()
        val b1Location = getNodeTransform(graph, b1).translation()
        val b2Location = getNodeTransform(graph, b2).translation()
        if (a2Location == b2Location)
          graph
        else {
          val center = getCenter(a2Location, b2Location)
          val gap = a2Location.distance(b2Location)
          val vector = (b2Location - a2Location).normalize()
          val (yaw, pitch) = getYawAndPitch(vector)
          val offset = Vector3(0f, 0f, -a1Location.z)

          val length = a1Location.distance(b1Location)

          val scale = Vector3((gap / length) + 0.05f, 1f, 1f)

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
