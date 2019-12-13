package compuquest_client

import compuquest_simulation.AbilityType

enum class ShopColumn {
  available,
  selected
}

data class SimpleAbility(
    val type: AbilityType,
    val level: Int
)

data class ShopState(
    val available: List<AbilityType>,
    val selected: List<AbilityType>,
    val availablePoints: Int,
    val existing: List<SimpleAbility>
)

val additionalAbilityPoints = 3

fun getAvailableAbilityPoints(existingAbilities: List<SimpleAbility>, creatureLevel: Int): Int =
    creatureLevel + additionalAbilityPoints - existingAbilities.map { it.type.purchaseCost }.sum()

data class ShopSelectionEvent(
    val column: ShopColumn,
    val ability: AbilityType
)

fun remainingPoints(state: ShopState): Int =
    state.availablePoints - state.selected.sumBy { it.purchaseCost }

fun handleAbilitySelectionEvent(event: ShopSelectionEvent, state: ShopState): ShopState =
    when (event.column) {
      ShopColumn.available ->
        if (event.ability.purchaseCost > remainingPoints(state))
          state
        else
          state.copy(
              available = state.available.minus(event.ability),
              selected = state.selected.plus(event.ability)
          )
      ShopColumn.selected ->
        state.copy(
            available = state.available.plus(event.ability),
            selected = state.selected.minus(event.ability)
        )
    }
