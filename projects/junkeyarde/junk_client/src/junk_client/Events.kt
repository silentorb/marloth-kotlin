package junk_client

import junk_simulation.Id

enum class EntityType {
  ability,
  creature
}

data class EntitySelectionEvent(
    val entityType: EntityType,
    val entityId: Id
)
