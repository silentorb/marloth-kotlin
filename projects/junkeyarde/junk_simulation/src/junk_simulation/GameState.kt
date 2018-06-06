package junk_simulation

enum class GameMode {
  abilitySelection,
  combat
}

enum class AbilitySelectionColumn {
  available,
  selected
}

data class AbilitySelectionState(
    val available: List<AbilityType>,
    val selected: List<AbilityType>
)

data class GameState(
    val world: World,
    val mode: GameMode,
    val abilitySelectionState: AbilitySelectionState?
)
