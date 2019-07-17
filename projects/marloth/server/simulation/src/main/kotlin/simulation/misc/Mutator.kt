package simulation.misc

import simulation.combat.ModifierOperation

data class ValueModifier(
    val value: Int,
    val operation: ModifierOperation,
    val subtype: Any?
)
