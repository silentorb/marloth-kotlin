package marloth.definition.templates

import marloth.definition.creatures
import mythic.ent.IdSource
import mythic.spatial.Vector3
import scenery.enums.AccessoryId
import scenery.enums.ModifierId
import scenery.enums.Text
import simulation.entities.*
import simulation.main.Hand
import simulation.main.HandAttachment


fun newMerchant(nextId: IdSource, position: Vector3, wares: List<Ware>): Hand {
  val character = newCharacter(
      nextId = nextId,
      position = position,
      definition = creatures.merchant,
      faction = 1
  )

  return character.copy(
      interactable = Interactable(
          primaryCommand = WidgetCommand(
              text = Text.menu_talk
          )
      ),
      attachments = character.attachments.plus(wares.map { ware ->
        HandAttachment(
            category = AttachmentTypeId.inventory,
            hand = Hand(
                ware = ware
            )
        )
      })
  )
}

val defaultWares: List<Ware> = listOf(
    Ware(
        type = AccessoryId.coldResistance,
        price = 10
    ),
    Ware(
        type = AccessoryId.fireResistance,
        price = 12
    ),
    Ware(
        type = AccessoryId.poisonResistance,
        price = 10
    )
)
