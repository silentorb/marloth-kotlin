package marloth.integration.clienting

import marloth.integration.misc.AppState
import silentorb.mythic.bloom.Box
import silentorb.mythic.bloom.emptyBox
import silentorb.mythic.ent.Id
import silentorb.mythic.spatial.Vector2i
import simulation.misc.Definitions

fun layoutPlayerGui(definitions: Definitions, appState: AppState): (Id, Vector2i) -> Box = { player, dimensions ->
  if (dimensions.x == 0 || dimensions.y == 0)
    emptyBox
  else {
    val world = appState.worlds.lastOrNull()
    marloth.clienting.gui.layoutPlayerGui(definitions, appState.options, appState.client, world, dimensions, player)
  }
}

fun layoutGui(definitions: Definitions, appState: AppState, dimensions: List<Vector2i>): Map<Id, Box> {
  val players = appState.client.players
  return if (players.none()) {
    mapOf()
  } else {
    players.zip(dimensions) { player, d -> player to layoutPlayerGui(definitions, appState)(player, d) }
        .associate { it }
  }
}
