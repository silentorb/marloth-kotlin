package marloth.integration.generation

import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.getNodeValue
import silentorb.mythic.ent.scenery.*
import silentorb.mythic.scenery.SceneProperties
import silentorb.mythic.spatial.Matrix
import simulation.misc.GameProperties

object SlotTypes {
  const val ground = "groundSlot"
  const val wall = "wallSlot"
}

data class Slot(
    val attributes: Set<String>,
    val biome: String?,
    val transform: Matrix,
)

object SlotAttributes {
  const val ground = "ground"
}

typealias SlotMap = Map<String, Slot>

tailrec fun getNodeBiome(graph: Graph, node: String): String? {
  val biome = getNodeValue<String>(graph, node, GameProperties.biome)
  return if (biome != null)
    biome
  else {
    val parent = getNodeValue<String>(graph, node, SceneProperties.parent)
    if (parent == null)
      null
    else
      getNodeBiome(graph, parent)
  }
}

fun gatherSlots(graph: Graph, attributes: Collection<String>): SlotMap =
    groupNodesWithCertainAttributes<String>(graph, attributes)
        .mapValues { (node, attributes) ->
          Slot(
              attributes = attributes.map { it.replace("dist", "") }.toSet(),
              biome = getNodeBiome(graph, node),
              transform = getAbsoluteNodeTransform(graph, node)
          )
        }
