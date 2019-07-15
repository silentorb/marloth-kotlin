package marloth.definition

import marloth.definition.data.accessories
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

//val propBodyAttributes = BodyAttributes(
//    resistance = 4f
//)
//
//val missileBodyAttributes = BodyAttributes(
//    resistance = 0f
//)
//
//val characterBodyAttributes = BodyAttributes(
//    resistance = 4f
////        resistance = 8f
//)

val staticDefinitions = Definitions(
    accessories = accessories
)
