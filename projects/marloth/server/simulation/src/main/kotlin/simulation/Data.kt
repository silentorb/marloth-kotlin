package simulation

import physics.BodyAttributes
import scenery.Sounds

class AbilityDefinitions {
//  val shoot = AbilityDefinition(
//      cooldown = 0.2f,
//      range = 15f,
//      maxSpeed = 35f
//  )
  val slowShoot = AbilityDefinition(
      cooldown = 0.8f,
      range = 20f,
      maxSpeed = 35f
  )
}

val abilityDefinitions = AbilityDefinitions()

class CharacterDefinitions {
  val player = CharacterDefinition(
      health = 200,
      abilities = listOf(),
      depictionType = DepictionType.child,
      maxSpeed = 5f,
      deathSound = Sounds.girlScream
  )

  val ally = CharacterDefinition(
      health = 100,
      abilities = listOf(abilityDefinitions.slowShoot),
      depictionType = DepictionType.child,
      maxSpeed = 3f,
      deathSound = Sounds.girlScream
  )

  val monster = CharacterDefinition(
      health = 100,
      abilities = listOf(abilityDefinitions.slowShoot),
      depictionType = DepictionType.person,
      maxSpeed = 3f,
      deathSound = Sounds.hogDeath
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
