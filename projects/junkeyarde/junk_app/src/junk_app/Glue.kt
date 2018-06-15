package junk_app

import junk_client.AppState
import junk_simulation.CommandType
import junk_simulation.GameCommand
import junk_client.GameMode
import junk_simulation.newWorld

fun finishCreatureCreation(state: AppState): AppState {
  return state.copy(
      client = state.client.copy(
          mode = GameMode.battle,
          shopState = null
      ),
      world = newWorld(state.client.shopState!!.selected)
  )
}

fun updateOutsideOfWorld(state: AppState, command: GameCommand): AppState =
    when (command.type) {
      CommandType.submit -> finishCreatureCreation(state)
      else -> throw Error("Unsupported command")
    }