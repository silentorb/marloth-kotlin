package silentorb.mythic.combat.spatial

import silentorb.mythic.accessorize.AccessoryName
import silentorb.mythic.combat.general.WeaponDefinition

data class SpatialCombatDefinitions (
    val weapons: Map<AccessoryName, WeaponDefinition>
)
