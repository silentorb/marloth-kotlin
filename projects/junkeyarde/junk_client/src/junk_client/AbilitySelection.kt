package junk_client

import junk_simulation.AbilityType
import junk_simulation.Character

enum class AbilitySelectionColumn {
  available,
  selected
}

data class SimpleAbility(
    val type: AbilityType,
    val level: Int
)

data class AbilitySelectionState(
    val available: List<AbilityType>,
    val selected: List<AbilityType>,
    val availablePoints: Int,
    val existing: List<SimpleAbility>
)

val additionalAbilityPoints = 3

fun getAvailableAbilityPoints(existingAbilities: List<SimpleAbility>, characterLevel: Int): Int =
    characterLevel + additionalAbilityPoints - existingAbilities.map { it.type.purchaseCost }.sum()

data class AbilitySelectionEvent(
    val column: AbilitySelectionColumn,
    val ability: AbilityType
)

fun remainingPoints(state: AbilitySelectionState): Int =
    state.availablePoints - state.selected.sumBy { it.purchaseCost }

fun handleAbilitySelectionEvent(event: AbilitySelectionEvent, state: AbilitySelectionState): AbilitySelectionState =
    when (event.column) {
      AbilitySelectionColumn.available ->
        if (event.ability.purchaseCost > remainingPoints(state))
          state
        else
          state.copy(
              available = state.available.minus(event.ability),
              selected = state.selected.plus(event.ability)
          )
      AbilitySelectionColumn.selected ->
        state.copy(
            available = state.available.plus(event.ability),
            selected = state.selected.minus(event.ability)
        )
    }
