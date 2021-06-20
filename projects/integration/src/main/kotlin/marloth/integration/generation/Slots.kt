package marloth.integration.generation

import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.scenery.getAbsoluteNodeTransform
import silentorb.mythic.ent.scenery.groupNodesWithCertainAttributes
import silentorb.mythic.spatial.Matrix
import simulation.misc.DistAttributes

object SlotTypes {
  const val ground = "groundSlot"
  const val wall = "wallSlot"
}

data class Slot(
    val attributes: Set<String>,
    val transform: Matrix,
)

object SlotAttributes {
  const val ground = "ground"
}

typealias SlotMap = Map<String, Slot>

fun gatherSlots(graph: Graph, attributes: Collection<String>): SlotMap =
    groupNodesWithCertainAttributes<String>(graph, attributes)
        .mapValues { (node, attributes) ->
          Slot(
              attributes = attributes
                  .map { it.replace("dist", "") }
                  .plus(getNodeBiomesWithInheritance(graph, node))
                  .toSet(),
              transform = getAbsoluteNodeTransform(graph, node)
          )
        }

fun mapSlotType(slot: Slot): String =
    if (slot.attributes.contains(SlotTypes.ground))
      DistAttributes.floor
    else
      DistAttributes.wall
