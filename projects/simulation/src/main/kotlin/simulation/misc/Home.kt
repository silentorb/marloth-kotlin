package simulation.misc

import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.scenery.getNodeAttributes
import silentorb.mythic.ent.scenery.getAbsoluteNodeTransform
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
  val spawners = getNodeAttributes(graph, GameAttributes.playerSpawn)
  val spawner = spawners.minByOrNull { path -> path.count { it == '.' } }
  return if (spawner != null)
    getAbsoluteNodeTransform(graph, spawner)
  else
    null
}

fun getNodePositionsByAttribute(graph: Graph, attribute: String): List<Matrix> =
    getNodeAttributes(graph, attribute)
        .map { getAbsoluteNodeTransform(graph, it) }
