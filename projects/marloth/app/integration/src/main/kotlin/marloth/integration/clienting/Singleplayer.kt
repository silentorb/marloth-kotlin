package marloth.integration.clienting

import marloth.clienting.input.defaultInputProfile
import marloth.integration.misc.AppState
import silentorb.mythic.haft.DeviceIndex

val updateAppStateForFirstNewPlayer: (AppState) -> AppState = { appState ->
  val client = appState.client
  val input = client.input
  val devicePlayerMap = input.devicePlayers
  val deviceMap = input.deviceMap
  val world = appState.worlds.lastOrNull()
  if (world != null && world.deck.players.any()) {
    val missingDevices = listOf(Pair(0, DeviceIndex.keyboard), Pair(1, DeviceIndex.mouse))
        .filter { device -> deviceMap.any { it.value == device.second } }
    if (missingDevices.any()) {
      val player = world.deck.players.keys.first()
      appState.copy(
          client = client.copy(
              input = input.copy(
                  deviceMap = deviceMap + missingDevices,
                  devicePlayers = devicePlayerMap + missingDevices.map { Pair(it.first, player) },
                  playerProfiles = input.playerProfiles + Pair(player, defaultInputProfile)
              ),
              players = client.players + player
          )
      )
    } else
      appState
  } else
    appState
}
