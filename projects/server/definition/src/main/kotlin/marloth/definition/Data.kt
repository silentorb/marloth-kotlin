package marloth.definition

import marloth.definition.data.*
import marloth.definition.misc.ClientDefinitions
import marloth.definition.particles.particleEffects
import marloth.definition.texts.englishTextResources
import marloth.scenery.enums.staticDamageTypes
import silentorb.mythic.ent.reflectPropertiesMap
import simulation.characters.CharacterDefinition
import simulation.misc.Definitions

fun staticDefinitions(clientDefinitions: ClientDefinitions): Definitions {
  val actionAccessories = actionAccessories()
  val weapons = actionAccessories
      .filterValues { it.weapon != null }
      .mapValues { it.value.weapon!! }

  return Definitions(
      actions = actionAccessories.mapValues { it.value.action },
      accessories = modifiers() + actionAccessories.mapValues { it.value.accessory },
      animations = clientDefinitions.animations + animationPlaceholders(),
      damageTypes = staticDamageTypes.toSet(),
      lightAttachments = clientDefinitions.lightAttachments,
      particleEffects = particleEffects(),
      professions = allProfessions() + reflectPropertiesMap<CharacterDefinition>(CharacterDefinitions),
      soundDurations = clientDefinitions.soundDurations,
      textLibrary = englishTextResources,
      weapons = weapons
  )
}
