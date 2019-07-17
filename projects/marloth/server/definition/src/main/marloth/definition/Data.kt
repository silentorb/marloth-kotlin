package marloth.definition

import marloth.definition.data.staticAccessories
import marloth.definition.data.staticModifiers
import simulation.entities.AbilityDefinition
import simulation.misc.Definitions

class AbilityDefinitions {
  //  val shoot = AbilityDefinition(
//      cooldown = 0.2f,
//      range = 15f,
//      maxSpeed = 35f
//  )
  val slowShoot = AbilityDefinition(
      cooldown = 0.8f,
      range = 20f,
      maxSpeed = 35f
  )
}

val abilityDefinitions = AbilityDefinitions()

val staticDefinitions = Definitions(
    accessories = staticAccessories,
    modifiers = staticModifiers
)
