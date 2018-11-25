package junk_client

import junk_simulation.ActionType
import junk_simulation.Id

enum class EntityType {
  ability,
  creature
}

data class EntitySelectionEvent(
    val entityType: EntityType,
    val entityId: Id
)

data class GlobalAbilityEvent(
    val actionType: ActionType,
    val abilityId: Id,
    val actor: Id
)