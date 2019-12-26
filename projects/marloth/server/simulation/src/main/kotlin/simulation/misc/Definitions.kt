package simulation.misc

import marloth.scenery.enums.ModifierId
import silentorb.mythic.scenery.Light
import silentorb.mythic.scenery.MeshName
import silentorb.mythic.combat.WeaponDefinition
import silentorb.mythic.accessorize.AccessoryDefinition
import silentorb.mythic.accessorize.AccessoryName
import simulation.entities.ActionDefinition
import silentorb.mythic.accessorize.ModifierDefinition
import silentorb.mythic.combat.DamageType

typealias AccessoryDefinitions = Map<AccessoryName, AccessoryDefinition>
typealias LightAttachmentMap = Map<MeshName, List<Light>>

data class Definitions(
    val accessories: Map<AccessoryName, AccessoryDefinition>,
    val actions: Map<AccessoryName, ActionDefinition>,
    val damageTypes: Set<DamageType>,
    val modifiers: Map<ModifierId, ModifierDefinition>,
    val lightAttachments: LightAttachmentMap,
    val weapons: Map<AccessoryName, WeaponDefinition>
)
