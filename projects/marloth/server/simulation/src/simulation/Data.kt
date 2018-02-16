package simulation

class AbilityDefinitions() {
  val shoot = AbilityDefinition(
      cooldown = 0.2f
  )

}

val abilityDefinitions = AbilityDefinitions()

class CharacterDefinitions() {
  val player = CharacterDefinition(
      health = 100,
      abilities = listOf(abilityDefinitions.shoot)
  )

  val monster = CharacterDefinition(
      health = 100,
      abilities = listOf(abilityDefinitions.shoot)
  )
}

val characterDefinitions = CharacterDefinitions()