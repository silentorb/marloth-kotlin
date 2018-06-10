package junk_client

import junk_client.views.ClientBattleState
import junk_simulation.Character
import junk_simulation.World
import junk_simulation.data.abilityLibrary
import junk_simulation.newWorld

enum class GameMode {
  abilitySelection,
  battle
}

data class AppState(
    val world: World?,
    val client: ClientState
)

fun updateAppState(state: AppState): AppState {
  return state.copy()
}

fun newAbilitySelectionState(existingAbilities: List<SimpleAbility>, characterLevel: Int) =
    AbilitySelectionState(
        available = abilityLibrary.filter { it.purchasable == true }.take(6),
        selected = listOf(),
        availablePoints = getAvailableAbilityPoints(existingAbilities, characterLevel),
        existing = existingAbilities
    )

fun newAppState(): AppState {
    return AppState(
      world = null,
      client = ClientState(
          mode = GameMode.abilitySelection,
          abilitySelectionState = newAbilitySelectionState(listOf(), 1),
          previousInput = mapOf(),
          battle = ClientBattleState()
      )
  )
}