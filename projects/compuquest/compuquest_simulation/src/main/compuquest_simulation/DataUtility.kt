package compuquest_simulation

inline fun <reified T : Any> reflectProperties(source: Any): List<T> = source::class.java.kotlin.members
    .filter {
      it.returnType.classifier == T::class }
    .map { it.call(source) as T }

fun getAbility(creature: Creature, abilityId: Id) =
    creature.abilities.first { it.id == abilityId }
