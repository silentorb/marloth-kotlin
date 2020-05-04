package marloth.definition.data

import marloth.scenery.enums.AccessoryId
import marloth.scenery.enums.SoundId
import marloth.scenery.enums.Text
import simulation.characters.CharacterDefinition
import simulation.entities.DepictionType
import simulation.characters.fieldOfView360

object CharacterDefinitions {

  val hogMan = CharacterDefinition(
      name = Text.unnamed,
      health = 100,
      accessories = listOf(AccessoryId.pistol),
      depictionType = DepictionType.person,
      speed = 2.5f,
      deathSound = SoundId.hogDeath,
      ambientSounds = listOf(

      )
  )

  val sentinel = CharacterDefinition(
      name = Text.unnamed,
      health = 100,
      accessories = listOf(AccessoryId.rocketLauncher, AccessoryId.entangle),
      depictionType = DepictionType.sentinel,
      speed = 0f,
      deathSound = null,
      ambientSounds = listOf(),
      fieldOfView = fieldOfView360
  )

  val hound = CharacterDefinition(
      name = Text.unnamed,
      health = 100,
      accessories = listOf(AccessoryId.claws, AccessoryId.dash),
      depictionType = DepictionType.hound,
      speed = 4f,
      deathSound = null,
      ambientSounds = listOf()
  )
}
