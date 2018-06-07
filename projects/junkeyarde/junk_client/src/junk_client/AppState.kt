package junk_client

import junk_simulation.World
import junk_simulation.data.abilityLibrary
import junk_simulation.newWorld

enum class GameMode {
  abilitySelection,
  combat
}

data class AppState(
    val world: World?,
    val mode: GameMode,
    val abilitySelectionState: AbilitySelectionState?
)

fun updateAppState(state: AppState): AppState {
  return state.copy()
}

fun newAbilitySelectionState() =
    AbilitySelectionState(
        available = abilityLibrary,
        selected = listOf()
    )

fun newAppState() =
    AppState(
        world = newWorld(),
        mode = GameMode.abilitySelection,
        abilitySelectionState = newAbilitySelectionState()
    )