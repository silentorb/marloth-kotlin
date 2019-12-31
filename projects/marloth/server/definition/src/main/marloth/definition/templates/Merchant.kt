package marloth.definition.templates

import marloth.definition.data.creatures
import marloth.definition.newCharacter
import silentorb.mythic.spatial.Vector3
import marloth.scenery.enums.AccessoryId
import marloth.scenery.enums.ClientCommand
import marloth.scenery.enums.Text
import simulation.entities.Attachment
import simulation.entities.AttachmentCategory
import silentorb.mythic.ent.IdSource
import simulation.entities.*
import simulation.main.Hand
import simulation.main.IdHand
import simulation.misc.Definitions

fun newMerchant(nextId: IdSource, definitions: Definitions, position: Vector3, wares: List<Ware>): List<IdHand> {
  val character = nextId()
  return newCharacter(nextId, character, definitions,
      definition = creatures.merchant,
      faction = 1,
      position = position
  )
      .plus(
          IdHand(
              id = character,
              hand = Hand(
                  interactable = Interactable(
                      primaryCommand = WidgetCommand(
                          text = Text.menu_talk,
                          clientCommand = ClientCommand.showMerchantView
                      )
                  )
              )
          )
      )
      .plus(
          wares.map { ware ->
            IdHand(
                id = nextId(),
                hand = Hand(
                    attachment = Attachment(
                        target = character,
                        category = AttachmentCategory.inventory
                    ),
                    ware = ware
                )
            )
          }
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
