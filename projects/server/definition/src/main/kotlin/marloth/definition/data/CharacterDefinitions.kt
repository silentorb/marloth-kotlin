package marloth.definition.data

import marloth.scenery.enums.AccessoryId
import marloth.scenery.enums.Sounds
import marloth.scenery.enums.Text
import simulation.characters.CharacterDefinition
import simulation.entities.DepictionType
import simulation.characters.fieldOfView360

object CharacterDefinitions {
  val ally = CharacterDefinition(
      name = Text.unnamed,
      health = 100,
      accessories = listOf(AccessoryId.pistol.name, AccessoryId.mobility.name),
      depictionType = DepictionType.child,
      maxSpeed = 2f,
      deathSound = Sounds.girlScream
  )

  val hogMan = CharacterDefinition(
      name = Text.unnamed,
      health = 100,
      accessories = listOf(AccessoryId.pistol.name, AccessoryId.mobility.name),
      depictionType = DepictionType.person,
      maxSpeed = 2.5f,
      deathSound = Sounds.hogDeath,
      ambientSounds = listOf(
//          Sounds.hogAmbient01.name,
//          Sounds.hogAmbient03.name
      )
  )

  val sentinel = CharacterDefinition(
      name = Text.unnamed,
      health = 100,
      accessories = listOf(AccessoryId.rocketLauncher.name),
      depictionType = DepictionType.sentinel,
      maxSpeed = 0f,
      deathSound = null,
      ambientSounds = listOf(),
      fieldOfView = fieldOfView360
  )

  val hound = CharacterDefinition(
      name = Text.unnamed,
      health = 100,
      accessories = listOf(AccessoryId.claws.name, AccessoryId.mobility.name),
      depictionType = DepictionType.hound,
      maxSpeed = 4f,
      deathSound = null,
      ambientSounds = listOf()
  )
}
