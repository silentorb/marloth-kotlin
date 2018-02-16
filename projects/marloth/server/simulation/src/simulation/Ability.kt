package simulation

data class AbilityDefinition(
    val cooldown: Float,
    val range: Float
)

class Ability(
    val definition: AbilityDefinition
) {
  var cooldown: Float = 0f
}

fun updateAbility(ability: Ability, delta: Float) {
  if (ability.cooldown > 0f) {
    ability.cooldown = Math.max(0f, ability.cooldown - delta)
  }
}

fun canUse(character: Character, ability: Ability): Boolean {
  return ability.cooldown == 0f
}

fun useAbility(ability: Ability) {
  ability.cooldown = ability.definition.cooldown
}