package junk_simulation.logic

import junk_simulation.Ability
import junk_simulation.AbilityTarget
import junk_simulation.Creature

fun hasResourcesFor(creature: Creature, ability: Ability): Boolean =
    ability.type.usageCost.all { cost ->
      val resource = creature.resources.firstOrNull { it.element == cost.key }
      resource != null && resource.value >= cost.value
    }

fun isReady(creature: Creature, ability: Ability): Boolean =
    ability.type.target != AbilityTarget.none &&
        ability.cooldown == 0 &&
        hasResourcesFor(creature, ability)