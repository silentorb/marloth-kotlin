package marloth.definition

import scenery.enums.AccessoryId
import scenery.enums.Sounds
import simulation.entities.CharacterDefinition
import simulation.entities.DepictionType

class Creatures {
  val player = CharacterDefinition(
      health = 200,
      accessories = listOf(),
      depictionType = DepictionType.child,
      maxSpeed = 4f,
      deathSound = Sounds.girlScream,
      damageMultipliers = mapOf()
  )

  val ally = CharacterDefinition(
      health = 100,
      accessories = listOf(AccessoryId.pistol.name),
      depictionType = DepictionType.child,
      maxSpeed = 2f,
      deathSound = Sounds.girlScream
  )

  val monster = CharacterDefinition(
      health = 100,
      accessories = listOf(AccessoryId.pistol.name),
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
      accessories = listOf(),
      depictionType = DepictionType.child,
      maxSpeed = 2f,
      deathSound = Sounds.girlScream
  )
}

val creatures = Creatures()
