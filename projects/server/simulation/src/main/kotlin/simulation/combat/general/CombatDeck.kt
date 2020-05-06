package simulation.combat.general

import silentorb.mythic.ent.Table
import silentorb.mythic.accessorize.Accessory
import silentorb.mythic.accessorize.Modifier

data class CombatDeck(
    val accessories: Table<Accessory>
)
