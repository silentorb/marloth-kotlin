package simulation.data

import scenery.Sounds
import simulation.CharacterDefinition
import simulation.DepictionType

class Creatures {
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
      deathSound = Sounds.hogDeath,
      ambientSounds = listOf(
          Sounds.hogAmbient01,
          Sounds.hogAmbient02,
          Sounds.hogAmbient03
      )
  )
}

val creatures = Creatures()