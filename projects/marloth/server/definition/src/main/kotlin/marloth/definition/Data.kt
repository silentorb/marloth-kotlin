package marloth.definition

import marloth.definition.data.staticAccessories
import marloth.definition.data.staticActionAccessories
import marloth.definition.data.staticModifiers
import marloth.definition.particles.particleEffects
import marloth.scenery.enums.staticDamageTypes
import simulation.misc.Definitions

fun staticDefinitions(clientDefinitions: ClientDefinitions): Definitions {
  val actionAccessories = staticActionAccessories()
  val weapons = actionAccessories
      .filterValues { it.weapon != null }
      .mapValues { it.value.weapon!! }

  return Definitions(
      actions = actionAccessories.mapValues { it.value.action },
      accessories = staticAccessories().plus(actionAccessories.mapValues { it.value.accessory }),
      animations = clientDefinitions.animations,
      damageTypes = staticDamageTypes.toSet(),
      lightAttachments = clientDefinitions.lightAttachments,
      modifiers = staticModifiers(),
      particleEffects = particleEffects(),
      soundDurations = clientDefinitions.soundDurations,
      weapons = weapons
  )
}
