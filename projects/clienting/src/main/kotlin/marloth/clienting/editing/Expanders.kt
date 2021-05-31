package marloth.clienting.editing

import silentorb.mythic.ent.*
import silentorb.mythic.ent.scenery.Expanders
import silentorb.mythic.ent.scenery.getAbsoluteNodeTransform
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
      if (connections.size != 2 || connections.any { !it.contains('+') })
        null
      else {
        val (a, b) = connections
        val (a1, a2) = a.split("+")
        val (b1, b2) = b.split("+")
        val accidentalLocation = getAbsoluteNodeTransform(graph, node).translation()
        val a1Location = getAbsoluteNodeTransform(graph, a1).translation() - accidentalLocation
        val a2Location = getAbsoluteNodeTransform(graph, a2).translation()
        val b1Location = getAbsoluteNodeTransform(graph, b1).translation() - accidentalLocation
        val b2Location = getAbsoluteNodeTransform(graph, b2).translation()
        if (a2Location == b2Location)
          graph
        else {
          val gap = a2Location.distance(b2Location)
          val gapCenter = getCenter(a2Location, b2Location)
          val length = a1Location.distance(b1Location)
          val lengthCenter = getCenter(a1Location, b1Location)
          val vector = (b2Location - a2Location).normalize()
          val (yaw, pitch) = getYawAndPitch(vector)
          val offset = -lengthCenter
          val scale = Vector3((gap / length) + 0.05f, 1f, 1f)

          replaceValues(graph,
              setOf(
                  Entry(node, SceneProperties.translation, gapCenter + offset),
                  Entry(node, SceneProperties.rotation, Vector3(0f, -pitch, yaw)),
                  Entry(node, SceneProperties.scale, scale),
              )
          )
        }
      }
    }
)
