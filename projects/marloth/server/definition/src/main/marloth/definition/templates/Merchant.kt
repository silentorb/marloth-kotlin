package marloth.definition.templates

import marloth.definition.creatures
import marloth.definition.newCharacter
import mythic.spatial.Vector3
import scenery.enums.AccessoryId
import scenery.enums.ClientCommand
import scenery.enums.Text
import simulation.entities.*
import simulation.main.Hand
import simulation.main.HandAttachment


fun newMerchant(position: Vector3, wares: List<Ware>): Hand {
  val character = newCharacter(
      definition = creatures.merchant,
      faction = 1,
      position = position
  )

  return character.copy(
      interactable = Interactable(
          primaryCommand = WidgetCommand(
              text = Text.menu_talk,
              clientCommand = ClientCommand.showMerchantView
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
