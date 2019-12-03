package marloth.definition

import marloth.definition.data.staticAccessories
import marloth.definition.data.staticActionAccessories
import marloth.definition.data.staticModifiers
import simulation.misc.Definitions

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

fun staticDefinitions(): Definitions {
  val actionAccessories = staticActionAccessories()
  return Definitions(
      actions = actionAccessories.mapValues { it.value.action },
      accessories = staticAccessories().plus(actionAccessories.mapValues { it.value.accessory }),
      modifiers = staticModifiers()
  )
}
