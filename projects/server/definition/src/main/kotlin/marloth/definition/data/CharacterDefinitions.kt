package marloth.definition.data

import marloth.scenery.enums.AccessoryId
import marloth.scenery.enums.SoundId
import marloth.scenery.enums.Text
import simulation.characters.CharacterDefinition
import simulation.entities.DepictionType
import simulation.characters.fieldOfView360

object CharacterDefinitions {
  val ally = CharacterDefinition(
      name = Text.unnamed,
      health = 100,
      accessories = listOf(AccessoryId.pistol, AccessoryId.mobility),
      depictionType = DepictionType.child,
      maxSpeed = 2f,
      deathSound = SoundId.girlScream
  )

  val hogMan = CharacterDefinition(
      name = Text.unnamed,
      health = 100,
      accessories = listOf(AccessoryId.pistol, AccessoryId.mobility),
      depictionType = DepictionType.person,
      maxSpeed = 2.5f,
      deathSound = SoundId.hogDeath,
      ambientSounds = listOf(
//          Sounds.hogAmbient01,
//          Sounds.hogAmbient03
      )
  )

  val sentinel = CharacterDefinition(
      name = Text.unnamed,
      health = 100,
      accessories = listOf(AccessoryId.rocketLauncher),
      depictionType = DepictionType.sentinel,
      maxSpeed = 0f,
      deathSound = null,
      ambientSounds = listOf(),
      fieldOfView = fieldOfView360
  )

  val hound = CharacterDefinition(
      name = Text.unnamed,
      health = 100,
      accessories = listOf(AccessoryId.claws, AccessoryId.mobility),
      depictionType = DepictionType.hound,
      maxSpeed = 4f,
      deathSound = null,
      ambientSounds = listOf()
  )
}
