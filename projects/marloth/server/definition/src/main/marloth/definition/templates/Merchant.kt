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
import simulation.misc.Definitions

fun newMerchant(definitions: Definitions, position: Vector3, wares: List<Ware>): Hand {
  val character = newCharacter(definitions,
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
            category = AttachmentCategory.inventory,
            hand = Hand(
                ware = ware
            )
        )
      })
  )
}

val defaultWares: List<Ware> = listOf(
    Ware(
        type = AccessoryId.resistanceCold.name,
        price = 10
    ),
    Ware(
        type = AccessoryId.resistanceFire.name,
        price = 12
    ),
    Ware(
        type = AccessoryId.resistancePoison.name,
        price = 10
    )
)
