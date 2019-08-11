package marloth.definition

import scenery.enums.Sounds
import simulation.combat.DamageType
import simulation.entities.CharacterDefinition
import simulation.entities.DepictionType

class Creatures {
  val player = CharacterDefinition(
      health = 200,
      abilities = listOf(),
      depictionType = DepictionType.child,
      maxSpeed = 5f,
      deathSound = Sounds.girlScream,
      damageMultipliers = mapOf()
  )

  val ally = CharacterDefinition(
      health = 100,
      abilities = listOf(abilityDefinitions.slowShoot),
      depictionType = DepictionType.child,
      maxSpeed = 2f,
      deathSound = Sounds.girlScream
  )

  val monster = CharacterDefinition(
      health = 100,
      abilities = listOf(abilityDefinitions.slowShoot),
      depictionType = DepictionType.person,
      maxSpeed = 2f,
      deathSound = Sounds.hogDeath,
      ambientSounds = listOf(
          Sounds.hogAmbient01,
          Sounds.hogAmbient03
      )
  )

  val merchant = CharacterDefinition(
      health = 100,
      abilities = listOf(),
      depictionType = DepictionType.child,
      maxSpeed = 2f,
      deathSound = Sounds.girlScream
  )
}

val creatures = Creatures()