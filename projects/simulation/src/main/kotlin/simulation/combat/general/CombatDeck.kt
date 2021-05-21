package simulation.combat.general

import silentorb.mythic.ent.Table
import simulation.accessorize.Accessory

data class CombatDeck(
    val accessories: Table<Accessory>
)
