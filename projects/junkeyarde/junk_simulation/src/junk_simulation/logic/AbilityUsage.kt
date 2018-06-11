package junk_simulation.logic

import junk_simulation.Ability
import junk_simulation.AbilityTarget
import junk_simulation.Character

fun hasResourcesFor(character: Character, ability: Ability): Boolean =
    ability.type.usageCost.all { cost ->
      val resource = character.resources.firstOrNull { it.element == cost.key }
      resource != null && resource.value >= cost.value
    }

fun isReady(character: Character, ability: Ability): Boolean =
    ability.type.target != AbilityTarget.none &&
        ability.cooldown == 0 &&
        hasResourcesFor(character, ability)