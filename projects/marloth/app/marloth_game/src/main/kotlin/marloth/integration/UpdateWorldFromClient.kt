package marloth.integration

import marloth.front.GameApp
import silentorb.mythic.ent.pipe
import simulation.happenings.Events
import silentorb.mythic.commanding.CharacterCommand
import simulation.main.WorldTransform
import simulation.updating.simulationDelta
import simulation.updating.updateWorld

fun updateWorldFromClient(app: GameApp, commands: List<CharacterCommand>, events: Events): WorldTransform {
  val animationDurations = app.client.renderer.animationDurations
  return pipe(
      updateWorld(animationDurations, commands, app.definitions, events, simulationDelta)
  )
}
