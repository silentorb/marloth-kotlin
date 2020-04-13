package marloth.definition.data

import marloth.scenery.enums.AccessoryId
import marloth.scenery.enums.Sounds
import simulation.entities.CharacterDefinition
import simulation.entities.DepictionType
import simulation.entities.fieldOfView360

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

  val hogMan = CharacterDefinition(
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

  val sentinel = CharacterDefinition(
      health = 100,
      accessories = listOf(AccessoryId.grenadeLauncher.name),
      depictionType = DepictionType.sentinel,
      maxSpeed = 0f,
      deathSound = null,
      ambientSounds = listOf(),
      fieldOfView = fieldOfView360
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
