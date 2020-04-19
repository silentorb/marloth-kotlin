package simulation.misc

import silentorb.mythic.ent.Id
import simulation.main.Deck

fun isAtHome(grid: MapGrid, deck: Deck): (Id) -> Boolean = { id ->
  val body = deck.bodies[id]
  if (body != null)
    grid.cells[getPointCell(body.position)]?.attributes?.contains(CellAttribute.home) ?: false
  else
    false
}
