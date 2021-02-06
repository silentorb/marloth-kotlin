package simulation.misc

import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.LooseGraph
import silentorb.mythic.ent.scenery.nodeAttributes
import silentorb.mythic.ent.scenery.getNodeTransform
import silentorb.mythic.spatial.Matrix

//fun isAtHome(grid: MapGrid, deck: Deck): (Id) -> Boolean = { id ->
////  val body = deck.bodies[id]
////  if (body != null)
////    grid.cells[getPointCell(body.position)]?.attributes?.contains(CellAttribute.home) ?: false
////  else
//  false
//}

//fun getPlayerStart(grid: MapGrid): Vector3i? =
//    grid.cells.entries.firstOrNull { it.value.attributes.contains(CellAttribute.home) }?.key ?: Vector3i.zero

fun getPlayerStart(graph: Graph): Matrix? {
  val spawners = nodeAttributes(graph, GameAttributes.playerSpawn)
  val spawner = spawners.minByOrNull { path -> path.count { it == '.' } }
  return if (spawner != null)
    getNodeTransform(graph, spawner)
  else
    null
}

fun getNodePositionsByAttribute(graph: LooseGraph, attribute: String): List<Matrix> =
    nodeAttributes(graph, attribute)
        .map { getNodeTransform(graph, it) }
