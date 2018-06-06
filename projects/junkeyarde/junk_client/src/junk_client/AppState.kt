package junk_client

import junk_simulation.World

enum class GameMode {
  abilitySelection,
  combat
}

data class AppState(
    val world: World?,
    val mode: GameMode,
    val abilitySelectionState: AbilitySelectionState?
)
