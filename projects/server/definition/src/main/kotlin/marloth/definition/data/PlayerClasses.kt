package marloth.definition.data

import marloth.scenery.enums.AccessoryId
import marloth.scenery.enums.Sounds
import simulation.entities.CharacterDefinition
import simulation.entities.DepictionType

object PlayerClasses {
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

  val debug = CharacterDefinition(
      health = 2000,
      accessories = listOf(AccessoryId.rocketLauncher.name),
      depictionType = DepictionType.child,
      maxSpeed = 8f,
      deathSound = Sounds.girlScream,
      damageMultipliers = mapOf()
  )
}

val characterClasses = mapOf(
    "soldier" to PlayerClasses.soldier,
    "magician" to PlayerClasses.magician,
    "debug" to PlayerClasses.debug
)
