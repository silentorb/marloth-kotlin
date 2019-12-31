package marloth.integration

import marloth.front.GameApp
import silentorb.mythic.ent.pipe
import silentorb.mythic.happenings.Events
import simulation.main.Deck
import simulation.main.WorldTransform
import simulation.updating.simulationDelta
import simulation.updating.updateWorld

fun updateWorldFromClient(app: GameApp, events: Events, previous: Deck): WorldTransform {
  return pipe(
      updateWorld(app.definitions, previous, events, simulationDelta)
  )
}
