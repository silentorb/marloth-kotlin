package marloth.definition

import marloth.definition.data.staticAccessories
import marloth.definition.data.staticActionAccessories
import marloth.definition.data.staticModifiers
import marloth.scenery.enums.DamageTypes
import silentorb.mythic.aura.SoundDurations
import silentorb.mythic.breeze.AnimationInfoMap
import simulation.misc.Definitions
import simulation.misc.LightAttachmentMap

class AbilityDefinitions {
  //  val shoot = AbilityDefinition(
//      cooldown = 0.2f,
//      range = 15f,
//      maxSpeed = 35f
//  )
//  val slowShoot = AbilityDefinition(
//      cooldown = 0.8f,
//      range = 20f,
//      maxSpeed = 35f
//  )
}

val abilityDefinitions = AbilityDefinitions()

val staticDamageTypes = DamageTypes.values().map { it.name }

data class ClientDefinitions(
    val animations: AnimationInfoMap,
    val lightAttachments: LightAttachmentMap,
    val soundDurations: SoundDurations
)

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
      modifiers = staticModifiers(),
      lightAttachments = clientDefinitions.lightAttachments,
      soundDurations = clientDefinitions.soundDurations,
      weapons = weapons
  )
}
