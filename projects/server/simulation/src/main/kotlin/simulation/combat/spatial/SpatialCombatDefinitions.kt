package simulation.combat.spatial

import silentorb.mythic.accessorize.AccessoryName
import simulation.combat.general.WeaponDefinition
import silentorb.mythic.performing.ActionDefinition

data class SpatialCombatDefinitions (
    val actions: Map<AccessoryName, ActionDefinition>,
    val weapons: Map<AccessoryName, WeaponDefinition>
)
