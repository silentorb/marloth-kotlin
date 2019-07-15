package simulation.entities

import mythic.ent.Id
import scenery.enums.AccessoryId
import scenery.enums.ModifierId
import scenery.enums.Text
import simulation.happenings.OrganizedEvents
import simulation.main.Deck

enum class AttachmentTypeId {
  ability,
  buff,
  equipped,
  inventory
}

data class Attachment(
    val target: Id,
    val category: AttachmentTypeId,
    val index: Int = 0
)

data class Modifier(
    val type: ModifierId,
    val strength: Int
)

data class Accessory(
    val type: AccessoryId
)

data class AccessoryDefinition(
    val name: Text,
    val modifiers: List<Modifier>
)

fun getTargetAttachments(deck: Deck, target: Id) =
    deck.attachments.filter { attachment ->
      attachment.value.target == target
    }

fun getAttachmentOfEntityType(deck: Deck, target: Id, type: ModifierId): Id? =
    getTargetAttachments(deck, target)
        .keys.firstOrNull {
      val buff = deck.buffs[it]
      buff?.type == type
    }

fun getTargetAttachmentsOfCategory(deck: Deck, target: Id, category: AttachmentTypeId): List<Id> =
    getTargetAttachments(deck, target)
        .filter { it.value.category == category }
        .mapNotNull { it.key }

fun updateAttachment(events: OrganizedEvents): (Id, Attachment) -> Attachment = { id, attachment ->
  val purchase = events.purchases.firstOrNull { it.ware == id }
  if (purchase != null)
    Attachment(
        category = AttachmentTypeId.ability,
        target = purchase.customer
    )
  else
    attachment
}
