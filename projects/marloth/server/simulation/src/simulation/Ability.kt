package simulation

data class AbilityDefinition(
    val cooldown: Float
)

class Ability(
    var cooldownMax: Float
) {
  var cooldown: Float = 0f
}

//enum class AbilityType {
//  shoot
//}

//typealias AbilityLibrary = Map<AbilityType, AbilityDefinition>

//fun createAbilities(): AbilityLibrary = mapOf(
//    AbilityType.shoot to AbilityDefinition(
//        1f
//    )
//)

fun createAbility(definition: AbilityDefinition) =
    Ability(definition.cooldown)

fun updateAbility(ability: Ability, delta: Float) {
  if (ability.cooldown > 0f) {
    ability.cooldown = Math.min(0f, ability.cooldown - delta)
  }
}