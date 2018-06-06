package junk_client

import junk_simulation.AbilityType
import junk_simulation.Character

enum class AbilitySelectionColumn {
  available,
  selected
}

data class AbilitySelectionState(
    val available: List<AbilityType>,
    val selected: List<AbilityType>
)

val additionalAbilityPoints = 3

fun getAvailableAbilityPoints(character: Character): Int =
    character.level + additionalAbilityPoints - character.abilities.map { it.type.purchaseCost }.sum()

data class AbilitySelectionEvent(
    val column: AbilitySelectionColumn,
    val ability: AbilityType
)

fun handleAbilitySelectionEvent(event: AbilitySelectionEvent, state: AbilitySelectionState): AbilitySelectionState =
    when (event.column) {
      AbilitySelectionColumn.available -> AbilitySelectionState(
          available = state.available.minus(event.ability),
          selected = state.selected.plus(event.ability)
      )
      AbilitySelectionColumn.selected -> AbilitySelectionState(
          available = state.available.plus(event.ability),
          selected = state.selected.minus(event.ability)
      )
    }
