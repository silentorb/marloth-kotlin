package marloth.game.debug

import marloth.game.front.RenderHook
import marloth.game.integration.AppState
import silentorb.mythic.debugging.getDebugBoolean

fun labRender(state: AppState): RenderHook = { sceneRenderer, scene ->
  val world = state.worlds.last()
  val deck = world.deck
  val renderer = sceneRenderer.renderer
  if (getDebugBoolean("DRAW_PHYSICS")) {
    drawBulletDebug(world.bulletState, deck.bodies[deck.players.keys.first()]!!.position)(sceneRenderer, scene)
  }
  val navMesh = state.worlds.last().navMesh
  if (navMesh != null)
    renderNavMesh(renderer, navMesh)

  conditionalDrawAiTargets(deck, renderer)
  conditionalDrawLights(scene.lights, renderer)
}
