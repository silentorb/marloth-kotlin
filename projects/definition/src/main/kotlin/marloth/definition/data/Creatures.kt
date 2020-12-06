package marloth.definition.data

import marloth.scenery.enums.AccessoryId
import marloth.scenery.enums.SoundId
import marloth.scenery.enums.Text
import simulation.characters.CharacterDefinition
import simulation.entities.DepictionType

object Creatures {
  val foodVendor = CharacterDefinition(
      name = Text.unnamed,
      health = 200,
      accessories = listOf(AccessoryId.rocketLauncher, AccessoryId.entangle),
      depictionType = DepictionType.child,
      speed = 12f,
      deathSound = SoundId.girlScream,
      damageMultipliers = mapOf()
  )
}
