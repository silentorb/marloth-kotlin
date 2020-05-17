package simulation.combat.general

import simulation.accessorize.AccessoryDefinition
import simulation.accessorize.AccessoryName

data class CombatDefinitions(
    val accessories: Map<AccessoryName, AccessoryDefinition>,
    val damageTypes: Set<DamageType>
)
