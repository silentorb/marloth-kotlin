package compuquest_simulation.logic

import compuquest_simulation.Ability
import compuquest_simulation.AbilityTarget
import compuquest_simulation.Creature

//fun hasResourcesFor(creature: Creature, ability: Ability): Boolean =
//    ability.type.usageCost.all { cost ->
//      val resource = creature.resources.firstOrNull { it.element == cost.key }
//      resource != null && resource.value >= cost.value
//    }

fun isReady(creature: Creature, ability: Ability): Boolean =
    ability.type.target != AbilityTarget.none &&
        ability.cooldown == 0
//        &&        hasResourcesFor(creature, ability)
