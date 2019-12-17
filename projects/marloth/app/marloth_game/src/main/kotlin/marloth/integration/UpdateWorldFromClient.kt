package marloth.integration

import marloth.front.GameApp
import silentorb.mythic.ent.pipe
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.CharacterCommand
import simulation.main.WorldTransform
import simulation.updating.simulationDelta
import simulation.updating.updateWorld

fun updateWorldFromClient(app: GameApp, events: Events): WorldTransform {
  val animationDurations = app.client.renderer.animationDurations
  return pipe(
      updateWorld(animationDurations, app.definitions, events, simulationDelta)
  )
}
