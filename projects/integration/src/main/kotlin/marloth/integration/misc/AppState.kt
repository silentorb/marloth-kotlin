package marloth.integration.misc

import marloth.clienting.AppOptions
import marloth.clienting.ClientState
import marloth.integration.front.GameHooks
import silentorb.mythic.quartz.TimestepState
import simulation.main.World

data class AppState(
    val client: ClientState,
    val options: AppOptions,
    val worlds: List<World>,
    val timestep: TimestepState,
    val hooks: GameHooks? = null
)
