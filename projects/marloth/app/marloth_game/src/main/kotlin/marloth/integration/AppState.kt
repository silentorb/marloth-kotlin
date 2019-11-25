package marloth.integration

import marloth.clienting.ClientState
import marloth.clienting.gui.gameIsActiveByClient
import mythic.ent.Id
import mythic.quartz.TimestepState
import mythic.quartz.updateTimestep
import simulation.main.World
import simulation.main.simulationDelta

data class AppState(
    val client: ClientState,
    val worlds: List<World>,
    val timestep: TimestepState
)

fun updateAppTimestep(timestepState: TimestepState) = updateTimestep(timestepState, simulationDelta.toDouble())
