package compuquest_app

import compuquest_client.AppState
import compuquest_simulation.CommandType
import compuquest_simulation.GameCommand
import compuquest_client.GameMode
import compuquest_simulation.newWorld

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
