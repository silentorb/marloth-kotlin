package marloth.integration.clienting

import marloth.clienting.gui.layoutPlayerGui
import marloth.integration.misc.AppState
import silentorb.mythic.bloom.Box
import silentorb.mythic.bloom.emptyBox
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.ent.Id
import silentorb.mythic.spatial.Vector2i
import simulation.misc.Definitions

fun getHudDebugInfo(appState: AppState): List<String> =
    listOfNotNull(
        if (getDebugBoolean("HUD_DRAW_LOOP_TIME")) {
          "Duration: " + String.format("%,d", appState.timestep.increment).padStart(10, ' ')
        } else
          null,
        if (getDebugBoolean("HUD_DRAW_FPS")) {
          val fps = appState.timestep.fps
          "FPS: " + String.format("%,d", fps).padStart(4, ' ')
        } else
          null,
    )

fun layoutIntegratedPlayerGui(definitions: Definitions, appState: AppState): (Id, Vector2i) -> Box = { player, dimensions ->
  if (dimensions.x == 0 || dimensions.y == 0)
    emptyBox
  else {
    val world = appState.worlds.lastOrNull()
    val hudDebugInfo = getHudDebugInfo(appState)
    layoutPlayerGui(definitions, appState.options, appState.client, world, dimensions, player, hudDebugInfo)
  }
}

fun layoutGui(definitions: Definitions, appState: AppState, dimensions: List<Vector2i>): Map<Id, Box> {
  val players = appState.client.players
  return if (players.none()) {
    mapOf()
  } else {
    players.zip(dimensions) { player, d -> player to layoutIntegratedPlayerGui(definitions, appState)(player, d) }
        .associate { it }
  }
}
