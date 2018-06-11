package junk_client

import junk_client.views.ClientBattleState
import junk_simulation.World
import junk_simulation.data.abilityLibrary

enum class GameMode {
  abilitySelection,
  battle
}

data class AppState(
    val world: World?,
    val client: ClientState
)

fun newAbilitySelectionState(existingAbilities: List<SimpleAbility>, characterLevel: Int) =
    ShopState(
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
          shopState = newAbilitySelectionState(listOf(), 1),
          previousInput = mapOf(),
          battle = ClientBattleState(
              selectedEntity = null
          )
      )
  )
}