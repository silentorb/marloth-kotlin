package simulation.misc

import scenery.enums.AccessoryId
import simulation.entities.AccessoryDefinition

typealias AccessoryDefinitions = Map<AccessoryId, AccessoryDefinition>

data class Definitions(
    val accessories: Map<AccessoryId, AccessoryDefinition>
)
