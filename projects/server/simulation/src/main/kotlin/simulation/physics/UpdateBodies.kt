package simulation.physics

import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import silentorb.mythic.physics.Body
import silentorb.mythic.spatial.Vector3
import simulation.entities.updateSpinnerRotation
import simulation.happenings.ReturnHome
import simulation.main.Deck
import simulation.misc.CellAttribute
import simulation.misc.MapGrid
import simulation.misc.floorOffset
import simulation.misc.getCellPoint

fun updateBodies(grid: MapGrid, deck: Deck, events: Events, delta: Float): (Id, Body) -> Body {
  val returnHomeEvents = events.filterIsInstance<ReturnHome>()
  return { id, body ->
    val a = updateSpinnerRotation(deck.spinners, delta)(id, body)
    if (returnHomeEvents.any { it.target == id }) {
      val home = grid.cells.entries
          .first { (_, cell) -> cell.attributes.contains(CellAttribute.home) }
      val newPosition = getCellPoint(home.key) + floorOffset + Vector3(0f, 0f, 2f)
      a.copy(
          position = newPosition
      )
    } else
      a
  }
}
