package simulation.entities

import mythic.ent.Id
import simulation.main.Deck
import simulation.main.simulationDelta
import simulation.input.CommandType
import simulation.input.Commands

data class AbilityDefinition(
    val cooldown: Float,
    val range: Float,
    val maxSpeed: Float
)

data class Ability(
    val id: Id,
    val definition: AbilityDefinition,
    val cooldown: Float = 0f
)

data class ActivatedAbility(
    val character: Id,
    val ability: Ability
)

data class NewAbility(
    val id: Id
)

fun updateCooldown(ability: Ability, isActivated: Boolean, delta: Float): Float {
  return if (isActivated)
    ability.definition.cooldown
  else if (ability.cooldown > 0f)
    Math.max(0f, ability.cooldown - delta)
  else
    0f
}

fun canUse(character: Character, ability: Ability): Boolean {
  return ability.cooldown == 0f
}

fun getActivatedAbilities(deck: Deck, commands: Commands): List<ActivatedAbility> {
  return commands.filter { it.type == CommandType.attack }
      .mapNotNull {
        val character = deck.characters[it.target]!!
        val ability = character.abilities.firstOrNull()
        if (ability != null && canUse(character, ability))
          ActivatedAbility(it.target, ability)
        else
          null
      }
}

fun updateAbilities(character: Character, activatedAbilities: List<Ability>): List<Ability> {
  return character.abilities.map { ability ->
    val isActivated = activatedAbilities.any { it.id == ability.id }
    ability.copy(
        cooldown = updateCooldown(ability, isActivated, simulationDelta)
    )
  }
}