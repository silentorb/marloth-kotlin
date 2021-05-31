package marloth.integration.generation

import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.scenery.getAbsoluteNodeTransform
import silentorb.mythic.ent.scenery.nodesWithAttribute
import silentorb.mythic.spatial.Matrix

data class Slot(
    val attributes: Set<String>,
    val transform: Matrix,
)

object SlotAttributes {
  const val ground = "ground"
}

typealias SlotMap = Map<String, Slot>

fun gatherSlots(graph: Graph): SlotMap =
    nodesWithAttribute(graph, "groundSlot").associateWith { key ->
      Slot(
          attributes = setOf(SlotAttributes.ground),
          transform = getAbsoluteNodeTransform(graph, key)
      )
    }
