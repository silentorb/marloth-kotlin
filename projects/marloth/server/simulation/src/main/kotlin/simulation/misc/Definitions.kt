package simulation.misc

import scenery.enums.ModifierId
import simulation.entities.AccessoryDefinition
import simulation.entities.AccessoryName
import simulation.entities.ActionDefinition
import simulation.entities.ModifierDefinition

typealias AccessoryDefinitions = Map<AccessoryName, AccessoryDefinition>

data class Definitions(
    val accessories: Map<AccessoryName, AccessoryDefinition>,
    val actions: Map<AccessoryName, ActionDefinition>,
    val modifiers: Map<ModifierId, ModifierDefinition>
)
