package compuquest_simulation

fun getAbility(creature: Creature, abilityId: Id) =
    creature.abilities.first { it.id == abilityId }
