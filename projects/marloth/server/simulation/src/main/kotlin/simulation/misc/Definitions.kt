package simulation.misc

import scenery.enums.AccessoryId
import scenery.enums.ModifierId
import simulation.entities.AccessoryDefinition
import simulation.entities.ModifierDefinition

typealias AccessoryDefinitions = Map<AccessoryId, AccessoryDefinition>

data class Definitions(
    val accessories: Map<AccessoryId, AccessoryDefinition>,
    val modifiers: Map<ModifierId, ModifierDefinition>
)
