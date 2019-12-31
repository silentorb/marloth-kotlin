package marloth.definition.data

import marloth.scenery.enums.AccessoryId
import marloth.scenery.enums.Sounds
import simulation.entities.CharacterDefinition
import simulation.entities.DepictionType

class Creatures {
  val player = CharacterDefinition(
      health = 200,
      accessories = listOf(),
      depictionType = DepictionType.child,
      maxSpeed = 3f,
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
      maxSpeed = 2.5f,
      deathSound = Sounds.hogDeath,
      ambientSounds = listOf(
          Sounds.hogAmbient01.name,
          Sounds.hogAmbient03.name
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
