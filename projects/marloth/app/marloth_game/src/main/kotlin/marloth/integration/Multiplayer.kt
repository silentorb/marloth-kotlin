package marloth.integration

import marloth.clienting.input.joiningGamepads
import marloth.clienting.input.newGamepadDeviceEntry
import marloth.generation.population.getPlayerCell
import marloth.generation.population.newPlayer
import simulation.main.addHandsToWorld

val updateAppStateForNewPlayers: (AppState) -> AppState = { appState ->
  val client = appState.client
  val inputState = client.input
  val deviceState = inputState.deviceStates.last()
  val deviceMap = inputState.deviceMap
  val world = appState.worlds.last()
  val deck = world.deck
  val realm = world.realm
  val grid = realm.grid

  val gamepadJoinCommands = joiningGamepads(deviceState.events, deviceMap)
  if (gamepadJoinCommands.none())
    appState
  else {
    val availablePlayers = world.deck.players.keys.filter { player ->
      deviceMap.none { it.value.player == player }
    }
    val newPlayerHands = gamepadJoinCommands.drop(availablePlayers.size)
        .map {
          newPlayer(world.definitions, grid, getPlayerCell(grid))
        }
    val nextWorld = addHandsToWorld(newPlayerHands)(world)
    val newPlayers = nextWorld.deck.players.keys.minus(deck.players.keys)
    val playersWithNewGamepads = newPlayers.plus(availablePlayers)
    val deviceEntries = gamepadJoinCommands.zip(playersWithNewGamepads, ::newGamepadDeviceEntry)
    appState.copy(
        client = client.copy(
            input = inputState.copy(
                deviceMap = deviceMap.plus(deviceEntries),
                playerProfiles = inputState.playerProfiles.plus(playersWithNewGamepads.associateWith { 1L })
            ),
            players = client.players.plus(newPlayers)
        ),
        worlds = appState.worlds.dropLast(1).plus(nextWorld)
    )
  }
}
