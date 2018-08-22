package simulation

data class AbilityDefinition(
    val cooldown: Float,
    val range: Float
)

data class Ability(
    val id: Id,
    val definition: AbilityDefinition,
    val cooldown: Float = 0f
)

fun updateCooldown(ability: Ability, isActivated: Boolean, delta: Float): Float {
  return if (isActivated)
    ability.cooldown
  else if (ability.cooldown > 0f)
    Math.max(0f, ability.cooldown - delta)
  else
    0f
}

fun canUse(character: Character, ability: Ability): Boolean {
  return ability.cooldown == 0f
}

data class ActivatedAbility(
    val character: Character,
    val ability: Ability
)

fun getActivatedAbilities(world: World, commands: Commands): List<ActivatedAbility> {
  return commands.filter { it.type == CommandType.attack }
      .mapNotNull {
        val character = world.characterTable[it.target]!!
        val ability = character.abilities.first()
        if (canUse(character, ability))
          ActivatedAbility(character, ability)
        else
          null
      }
}