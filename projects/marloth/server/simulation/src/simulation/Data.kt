package simulation

class AbilityDefinitions() {
  val shoot = AbilityDefinition(
      cooldown = 0.2f,
      range = 10f
  )
}

val abilityDefinitions = AbilityDefinitions()

class CharacterDefinitions() {
  val player = CharacterDefinition(
      health = 200,
      abilities = listOf(abilityDefinitions.shoot)
  )

  val monster = CharacterDefinition(
      health = 100,
      abilities = listOf(abilityDefinitions.shoot)
  )
}

val characterDefinitions = CharacterDefinitions()