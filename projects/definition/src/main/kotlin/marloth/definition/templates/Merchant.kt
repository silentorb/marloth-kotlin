package marloth.definition.templates

import marloth.scenery.enums.AccessoryId
import simulation.entities.*

//fun newMerchant(nextId: IdSource, definitions: Definitions, position: Vector3, wares: List<Ware>): List<IdHand> {
//  val character = nextId()
//  return newCharacter(nextId, character, definitions,
//      definition = Creatures.merchant,
//      faction = 1,
//      position = position
//  )
//      .plus(
//          IdHand(
//              id = character,
//              hand = Hand(
//                  interactable = Interactable(
//                      primaryCommand = WidgetCommand(
//                          text = Text.menu_talk,
//                          clientCommand = ClientCommand.showMerchantView
//                      )
//                  )
//              )
//          )
//      )
//      .plus(
//          wares.map { ware ->
//            IdHand(
//                id = nextId(),
//                hand = Hand(
//                    attachment = Attachment(
//                        target = character,
//                        category = AttachmentCategory.inventory
//                    ),
//                    ware = ware
//                )
//            )
//          }
//      )
//}

//val defaultWares: List<Ware> = listOf(
//    Ware(
//        type = AccessoryId.resistanceCold,
//        price = 10
//    ),
//    Ware(
//        type = AccessoryId.resistanceFire,
//        price = 12
//    ),
//    Ware(
//        type = AccessoryId.resistancePoison,
//        price = 10
//    )
//)
