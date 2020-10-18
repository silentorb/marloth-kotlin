package simulation.misc

import silentorb.mythic.ent.Id
import silentorb.mythic.spatial.Vector3i
import simulation.main.Deck

fun isAtHome(grid: MapGrid, deck: Deck): (Id) -> Boolean = { id ->
  val body = deck.bodies[id]
  if (body != null)
    grid.cells[getPointCell(body.position)]?.attributes?.contains(CellAttribute.home) ?: false
  else
    false
}

fun getPlayerStart(grid: MapGrid): Vector3i? =
    grid.cells.entries.firstOrNull { it.value.attributes.contains(CellAttribute.home) }?.key ?: Vector3i.zero
