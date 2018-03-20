package simulation

import scenery.DepictionType

class AbilityDefinitions {
  val shoot = AbilityDefinition(
      cooldown = 0.2f,
      range = 10f
  )
  val slowShoot = AbilityDefinition(
      cooldown = 0.4f,
      range = 10f
  )
}

val abilityDefinitions = AbilityDefinitions()

class CharacterDefinitions {
  val player = CharacterDefinition(
      health = 200,
      abilities = listOf(abilityDefinitions.shoot),
      depictionType = DepictionType.character
  )

  val monster = CharacterDefinition(
      health = 100,
      abilities = listOf(abilityDefinitions.slowShoot),
      depictionType = DepictionType.monster
  )
}

val characterDefinitions = CharacterDefinitions()