package simulation

import physics.BodyAttributes

class AbilityDefinitions {
  val shoot = AbilityDefinition(
      cooldown = 0.2f,
      range = 15f
  )
  val slowShoot = AbilityDefinition(
      cooldown = 0.8f,
      range = 15f
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

val doodadBodyAttributes = BodyAttributes(
    resistance = 4f
)

val missileBodyAttributes = BodyAttributes(
    resistance = 0f
)

val characterBodyAttributes = BodyAttributes(
    resistance = 4f
//        resistance = 8f
)
