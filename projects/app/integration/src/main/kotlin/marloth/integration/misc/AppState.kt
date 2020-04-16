package marloth.integration.misc

import marloth.clienting.ClientState
import silentorb.mythic.quartz.TimestepState
import silentorb.mythic.quartz.updateTimestep
import simulation.main.World
import simulation.updating.simulationDelta

data class AppState(
    val client: ClientState,
    val worlds: List<World>,
    val timestep: TimestepState
)

fun updateAppTimestep(timestepState: TimestepState) = updateTimestep(timestepState, simulationDelta.toDouble())
