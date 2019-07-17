package simulation.entities

import mythic.ent.Id
import scenery.enums.*
import simulation.happenings.Action
import simulation.happenings.OrganizedEvents
import simulation.main.Deck
import simulation.misc.ValueModifier

enum class AttachmentTypeId {
  ability,
  buff,
  equipped,
  inventory
}

data class Attachment(
    val target: Id,
    val category: AttachmentTypeId,
    val index: Int = 0,
    val source: Id = 0L
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

data class ModifierDefinition(
    val name: Text,
    val type: ModifierType = ModifierType._notSpecified,
    val direction: ModifierDirection = ModifierDirection.none,
    val overTime: Action? = null,
    val valueModifier: ValueModifier? = null
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
