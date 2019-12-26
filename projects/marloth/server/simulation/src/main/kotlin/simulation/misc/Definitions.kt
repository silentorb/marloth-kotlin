package simulation.misc

import marloth.scenery.enums.ModifierId
import silentorb.mythic.scenery.Light
import silentorb.mythic.scenery.MeshName
import simulation.entities.AccessoryDefinition
import simulation.entities.AccessoryName
import simulation.entities.ActionDefinition
import simulation.entities.ModifierDefinition

typealias AccessoryDefinitions = Map<AccessoryName, AccessoryDefinition>
typealias LightAttachmentMap = Map<MeshName, List<Light>>

data class Definitions(
    val accessories: Map<AccessoryName, AccessoryDefinition>,
    val actions: Map<AccessoryName, ActionDefinition>,
    val modifiers: Map<ModifierId, ModifierDefinition>,
    val lightAttachments: LightAttachmentMap
)
