package simulation.combat.general

import silentorb.mythic.accessorize.AccessoryDefinition
import silentorb.mythic.accessorize.AccessoryName

data class CombatDefinitions(
    val accessories: Map<AccessoryName, AccessoryDefinition>,
    val damageTypes: Set<DamageType>
)
