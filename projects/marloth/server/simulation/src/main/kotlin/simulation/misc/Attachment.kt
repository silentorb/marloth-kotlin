package simulation.misc

import mythic.ent.Id
import simulation.main.Deck

enum class AttachmentTypeId {
  buff,
  equipped,
  inventory
}

data class Attachment(
    val target: Id,
    val category: AttachmentTypeId,
    val index: Int = 0
)

data class Buff(
    val strength: Int
)

fun getTargetAttachments(deck: Deck, target: Id) =
    deck.attachments.filter { attachment ->
      attachment.value.target == target
    }

fun getAttachmentOfEntityType(deck: Deck, target: Id, type: EntityTypeName): Id? =
    getTargetAttachments(deck, target)
        .keys.firstOrNull {
      val entity = deck.entities[it]
      entity?.type == type
    }

fun getTargetAttachmentsOfCategory(deck: Deck, target: Id, category: AttachmentTypeId): List<Id> =
    getTargetAttachments(deck, target)
        .filter { it.value.category == category }
        .mapNotNull { it.key }
