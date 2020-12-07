package marloth.definition.data

import marloth.scenery.enums.AccessoryId
import marloth.scenery.enums.Text
import simulation.characters.CharacterDefinition
import simulation.entities.DepictionType
import simulation.entities.Ware

object Creatures {
  val foodVendor = CharacterDefinition(
      name = Text.unnamed,
      depictionType = DepictionType.child,
      wares = listOf(
          Ware(
              type = AccessoryId.cookie,
              price = 10,
          )
      )
  )
}
