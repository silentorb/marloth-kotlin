package silentorb.mythic.combat.general

enum class ValueModifierDirection(val value: Int) {
  minus(-1),
  plus(1)
}

data class ValueModifier(
    val operation: ModifierOperation,
    val direction: ValueModifierDirection,
    val subtype: Any?
)

fun resolveValueModifier(modifier: ValueModifier, value: Int): Int =
    value * modifier.direction.value
