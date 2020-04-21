package marloth.definition.data

import marloth.scenery.enums.AccessoryId
import marloth.scenery.enums.Sounds
import simulation.entities.CharacterDefinition
import simulation.entities.DepictionType
import simulation.entities.fieldOfView360

object Creatures {
  val soldier = CharacterDefinition(
      health = 200,
      accessories = listOf(AccessoryId.shotgun.name),
      depictionType = DepictionType.child,
      maxSpeed = 5f,
      deathSound = Sounds.girlScream,
      damageMultipliers = mapOf()
  )

  val magician = CharacterDefinition(
      health = 200,
      accessories = listOf(AccessoryId.rocketLauncher.name),
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
      accessories = listOf(AccessoryId.rocketLauncher.name),
      depictionType = DepictionType.sentinel,
      maxSpeed = 0f,
      deathSound = null,
      ambientSounds = listOf(),
      fieldOfView = fieldOfView360
  )

  val hound = CharacterDefinition(
      health = 100,
      accessories = listOf(AccessoryId.claws.name),
      depictionType = DepictionType.hound,
      maxSpeed = 4f,
      deathSound = null,
      ambientSounds = listOf()
  )

  val merchant = CharacterDefinition(
      health = 100,
      accessories = listOf(),
      depictionType = DepictionType.child,
      maxSpeed = 2f,
      deathSound = Sounds.girlScream
  )
}
