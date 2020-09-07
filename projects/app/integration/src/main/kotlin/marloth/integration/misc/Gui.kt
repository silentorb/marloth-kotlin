package marloth.integration.misc

import silentorb.mythic.bloom.Box
import silentorb.mythic.ent.Id
import silentorb.mythic.spatial.Vector2i
import simulation.misc.Definitions

fun layoutPlayerGui(definitions: Definitions, appState: AppState): (Id, Vector2i) -> Box = { player, dimensions ->
  val world = appState.worlds.lastOrNull()
  marloth.clienting.menus.layoutPlayerGui(definitions, appState.client, world, dimensions, player)
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
