package junk_app

import junk_client.AppState
import junk_client.CommandType
import junk_client.GameMode
import junk_simulation.newWorld

fun finishCharacterCreation(state: AppState): AppState {
  return state.copy(
      client = state.client.copy(
          mode = GameMode.battle,
          shopState = null
      ),
      world = newWorld(state.client.shopState!!.selected)
  )
}

fun updateOverlap(state: AppState, command: CommandType): AppState =
    when (command) {
      CommandType.submit -> finishCharacterCreation(state)
      else -> throw Error("Unsupported command")
    }