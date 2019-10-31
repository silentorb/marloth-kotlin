package compuquest_client

import compuquest_client.views.ClientBattleState
import compuquest_simulation.World
import compuquest_simulation.data.abilityLibrary
import compuquest_simulation.newWorld
import mythic.bloom.next.newBloomState

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

fun newClientBattleState() =
    ClientBattleState(
        selectedEntity = null,
        flicker = 0f
    )

fun newAppState(): AppState {
  return AppState(
      world = null,
      client = ClientState(
          mode = GameMode.abilitySelection,
          shopState = newAbilitySelectionState(listOf(), 1),
          input = newInputState(),
          battle = newClientBattleState(),
          render = newRenderState(),
          bloomState = newBloomState()
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
          input = newInputState(),
          battle = newClientBattleState(),
          render = newRenderState(),
          bloomState = newBloomState()
      )
  )
}
