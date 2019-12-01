package marloth.integration

import marloth.front.GameApp
import mythic.ent.pipe
import simulation.happenings.Events
import simulation.input.Command
import simulation.main.WorldTransform
import simulation.updating.simulationDelta
import simulation.updating.updateWorld

fun updateWorldFromClient(app: GameApp, commands: List<Command>, events: Events): WorldTransform {
  val animationDurations = app.client.renderer.animationDurations
  return pipe(
      updateWorld(app.bulletState, animationDurations, commands, app.definitions, events, simulationDelta)
  )
}
