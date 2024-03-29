package marloth.integration.debug

import marloth.integration.front.RenderSceneHook
import marloth.integration.misc.AppState
import silentorb.mythic.debugging.getDebugBoolean

fun labRender(state: AppState): RenderSceneHook = { sceneRenderer, scene ->
  val world = state.worlds.last()
  val deck = world.deck
  val renderer = sceneRenderer.renderer
  if (getDebugBoolean("DRAW_PHYSICS")) {
    drawBulletDebug(world.bulletState, deck.bodies[deck.players.keys.first()]!!.position)(sceneRenderer, scene)
  }
  val navMesh = state.worlds.last().navigation?.mesh
  if (navMesh != null)
    renderNavMesh(sceneRenderer, navMesh)

  conditionalDrawAiTargets(deck, renderer)
  conditionalDrawLights(scene.lights, renderer)
}
