package generation.architecture.engine

import generation.general.CellDirection
import generation.general.Side
import generation.general.StandardHeights
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.getNodeValue
import silentorb.mythic.ent.getNodeValues
import silentorb.mythic.ent.scenery.nodeHasAttribute
import simulation.misc.GameAttributes
import simulation.misc.GameProperties

fun gatherSides(sideGroups: Map<String, Set<String>>, graph: Graph, sideNodes: Collection<String>,
                nonTraversableBlockSides: Collection<String>): List<Pair<CellDirection, Side?>> {
  val sides = sideNodes
      .mapNotNull { node ->
        val mine = getNodeValue<String>(graph, node, GameProperties.mine)
        val initialOther = getNodeValues<String>(graph, node, GameProperties.other)
        val isEssential = nodeHasAttribute(graph, node, GameAttributes.isEssentialSide)
        val other = expandSideGroups(sideGroups, initialOther)
        val cellDirection = getCellDirection(graph, node)
        if (cellDirection == null)
          null
        else if (mine == null || other.none())
          cellDirection to null
        else {
          val height = getNodeValue<Int>(graph, node, GameProperties.sideHeight) ?: StandardHeights.first
          cellDirection to Side(
              mine = mine,
              other = other.toSet(),
              height = height,
              isTraversable = !nonTraversableBlockSides.contains(mine),
              isEssential = isEssential,
              greedy = nodeHasAttribute(graph, node, GameAttributes.greedy)
          )
        }
      }

  return if (sides.any { it.second?.isEssential == true })
    sides.map { (cellDirection, side) ->
      cellDirection to side?.copy(
          canMatchEssential = side.isEssential
      )
    }
  else
    sides
}
