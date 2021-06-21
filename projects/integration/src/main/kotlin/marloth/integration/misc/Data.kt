package marloth.integration.misc

import marloth.clienting.Client
import marloth.clienting.editing.marlothEditorProperties
import marloth.clienting.gatherMeshLights
import marloth.clienting.gui.gatherInputEventTypeNames
import marloth.definition.data.*
import marloth.definition.misc.loadMarlothDefinitions
import marloth.definition.misc.staticDamageTypes
import marloth.definition.particles.particleEffects
import marloth.definition.texts.englishTextResources
import silentorb.mythic.editing.extractPropertiesSerialization
import silentorb.mythic.lookinglass.mapAnimationInfo
import simulation.misc.Definitions

fun staticDefinitions(client: Client): Definitions {
  val propertiesSerialization = extractPropertiesSerialization(marlothEditorProperties)
  val actionAccessories = actionAccessories()
  val weapons = actionAccessories
      .filterValues { it.weapon != null }
      .mapValues { it.value.weapon!! }

  return Definitions(
      actions = actionAccessories.mapValues { it.value.action },
      accessories = modifiers() + actionAccessories.mapValues { it.value.accessory } + accessories(),
      animations = mapAnimationInfo(client.renderer.armatures) + animationPlaceholders(),
      applicationInfo = loadApplicationInfo(),
      damageTypes = staticDamageTypes.toSet(),
      lightAttachments = gatherMeshLights(client.renderer.meshes),
      particleEffects = particleEffects(),
      professions = allProfessions() + monsterDefinitions(),
      selectableAccessories = selectableAccessories(),
      soundDurations = client.soundLibrary.mapValues { it.value.duration },
      textLibrary = englishTextResources,
      weapons = weapons,
      resourceInfo = client.resourceInfo,
      graphs = loadMarlothDefinitions(propertiesSerialization),
      propertiesSerialization = propertiesSerialization,
      inputEventTypeNames = gatherInputEventTypeNames(client),
  )
}
