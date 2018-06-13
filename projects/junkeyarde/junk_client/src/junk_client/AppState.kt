package junk_client

import junk_client.views.ClientBattleState
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

fun newAbilitySelectionState(existingAbilities: List<SimpleAbility>, creatureLevel: Int) =
    ShopState(
        available = abilityLibrary.filter { it.purchasable == true }.take(6),
        selected = listOf(),
        availablePoints = getAvailableAbilityPoints(existingAbilities, creatureLevel),
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

data class LabConfig(
    val playerAbilities: List<String>
)

fun newLabAppState(config: LabConfig): AppState {
  val abilities = config.playerAbilities.map { name -> abilityLibrary.first { it.name == name } }
  return AppState(
      world = newWorld(abilities),
      client = ClientState(
          mode = GameMode.battle,
          shopState = null,
          previousInput = mapOf(),
          battle = ClientBattleState(
              selectedEntity = null
          )
      )
  )
}