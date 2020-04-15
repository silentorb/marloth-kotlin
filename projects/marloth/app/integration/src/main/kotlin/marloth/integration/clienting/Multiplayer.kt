package marloth.integration.clienting

import marloth.clienting.input.defaultInputProfile
import marloth.clienting.input.joiningGamepads
import marloth.integration.misc.AppState
import marloth.generation.population.getPlayerCell
import marloth.generation.population.newPlayer
import silentorb.mythic.haft.DeviceIndex
import simulation.main.addEntitiesToWorldDeck

val updateAppStateForNewPlayers: (AppState) -> AppState = { appState ->
  val client = appState.client
  val inputState = client.input
  val deviceState = inputState.deviceStates.last()
  val deviceMap = inputState.deviceMap
  val devicePlayers = inputState.devicePlayers
  val world = appState.worlds.last()
  val deck = world.deck
  val realm = world.realm
  val grid = realm.grid

  val gamepadJoinCommands = joiningGamepads(deviceState.events, deviceMap)
  if (gamepadJoinCommands.none())
    appState
  else {
    val availablePlayers = world.deck.players.keys.filter { player ->
      devicePlayers.none { it.value == player }
    }

    val nextWorld = addEntitiesToWorldDeck(world) { nextId ->
      gamepadJoinCommands.drop(availablePlayers.size)
          .flatMap {
            newPlayer(nextId, world.definitions, grid, getPlayerCell(grid))
          }
    }
    val newPlayers = nextWorld.deck.players.keys.minus(deck.players.keys)
    val playersWithNewGamepads = newPlayers.plus(availablePlayers)
//    val deviceEntries = gamepadJoinCommands.zip(playersWithNewGamepads, ::newGamepadDeviceEntry)
    appState.copy(
        client = client.copy(
            input = inputState.copy(
                deviceMap = deviceMap + gamepadJoinCommands.associateWith { DeviceIndex.gamepad },
                devicePlayers = devicePlayers + gamepadJoinCommands.zip(playersWithNewGamepads).associate { Pair(it.first, it.second) },
                playerProfiles = inputState.playerProfiles + playersWithNewGamepads.associateWith { defaultInputProfile }
            ),
            players = client.players.plus(newPlayers)
        ),
        worlds = appState.worlds.dropLast(1).plus(nextWorld)
    )
  }
}
