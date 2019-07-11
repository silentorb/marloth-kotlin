package marloth.definition.templates

import scenery.enums.BuffId
import marloth.definition.creatures
import mythic.ent.IdSource
import mythic.spatial.Vector3
import scenery.enums.Text
import simulation.entities.*
import simulation.main.Hand
import simulation.main.HandAttachment
import simulation.misc.*

data class NewWare(
    val type: EntityTypeName,
    val price: Int
)

fun newMerchant(nextId: IdSource, position: Vector3, wares: List<NewWare>): Hand {
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
                entity = Entity(
                    type = ware.type
                ),
                ware = Ware(
                    price = ware.price
                )
            )
        )
      })
  )
}

val defaultWares: List<NewWare> = listOf(
    NewWare(
        type = BuffId.receivedBurningMultiplier.name,
        price = 10
    ),
    NewWare(
        type = BuffId.receivedChilledMultiplier.name,
        price = 12
    )
)
