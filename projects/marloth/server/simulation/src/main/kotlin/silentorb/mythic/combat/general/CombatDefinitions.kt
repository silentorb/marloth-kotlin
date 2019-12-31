package silentorb.mythic.combat.general

import marloth.scenery.enums.ModifierId
import silentorb.mythic.accessorize.AccessoryDefinition
import silentorb.mythic.accessorize.AccessoryName
import silentorb.mythic.accessorize.ModifierDefinition

data class CombatDefinitions(
    val accessories: Map<AccessoryName, AccessoryDefinition>,
    val damageTypes: Set<DamageType>,
    val modifiers: Map<ModifierId, ModifierDefinition>
)
