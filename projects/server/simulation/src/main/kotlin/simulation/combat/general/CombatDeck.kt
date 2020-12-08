package simulation.combat.general

import silentorb.mythic.ent.Table
import simulation.accessorize.AccessoryStack

data class CombatDeck(
    val accessories: Table<AccessoryStack>
)
