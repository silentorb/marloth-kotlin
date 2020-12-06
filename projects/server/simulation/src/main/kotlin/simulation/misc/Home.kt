package simulation.misc

import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.filterByAttribute
import silentorb.mythic.ent.firstOrNullWithAttribute
import silentorb.mythic.ent.scenery.getNodeTransform
import silentorb.mythic.spatial.Matrix
import silentorb.mythic.spatial.Vector3i
import simulation.main.Deck

fun isAtHome(grid: MapGrid, deck: Deck): (Id) -> Boolean = { id ->
  val body = deck.bodies[id]
  if (body != null)
    grid.cells[getPointCell(body.position)]?.attributes?.contains(CellAttribute.home) ?: false
  else
    false
}

fun getPlayerStart(graph: Graph): Matrix? {
  val spawners = filterByAttribute(graph, GameAttributes.playerSpawn)
  val spawner = spawners.minByOrNull { path -> path.count { it == '.' } }
  return if (spawner != null)
    getNodeTransform(graph, spawner)
  else
    null
}

fun getNodePositionsByAttribute(graph: Graph): List<Matrix> =
    filterByAttribute(graph, GameAttributes.playerSpawn)
        .map { getNodeTransform(graph, it) }
