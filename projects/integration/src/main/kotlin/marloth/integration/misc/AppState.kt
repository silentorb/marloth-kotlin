package marloth.integration.misc

import marloth.clienting.AppOptions
import marloth.clienting.ClientState
import marloth.integration.front.GameHooks
import silentorb.mythic.quartz.TimestepState
import silentorb.mythic.quartz.updateTimestep
import simulation.main.World
import simulation.updating.simulationDelta

data class AppState(
    val client: ClientState,
    val options: AppOptions,
    val worlds: List<World>,
    val timestep: TimestepState,
    val hooks: GameHooks? = null
)
