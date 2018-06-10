package junk_client

import junk_simulation.Character
import junk_simulation.World
import junk_simulation.data.abilityLibrary
import junk_simulation.newWorld

enum class GameMode {
  abilitySelection,
  combat
}

data class AppState(
    val world: World?,
    val client: ClientState
)

fun updateAppState(state: AppState): AppState {
  return state.copy()
}

fun newAbilitySelectionState(player: Character) =
    AbilitySelectionState(
        available = abilityLibrary.filter { it.purchasable == true }.take(6),
        selected = listOf(),
        availablePoints = getAvailableAbilityPoints(player)
    )

fun newAppState(): AppState {
  val world = newWorld()
  return AppState(
      world = world,
      client = ClientState(
          mode = GameMode.abilitySelection,
          abilitySelectionState = newAbilitySelectionState(world.player),
          previousInput = mapOf()
      )
  )
}