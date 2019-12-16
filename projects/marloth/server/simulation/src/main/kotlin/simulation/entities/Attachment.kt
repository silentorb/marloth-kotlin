package simulation.entities

import marloth.scenery.enums.ModifierDirection
import marloth.scenery.enums.ModifierId
import marloth.scenery.enums.ModifierType
import marloth.scenery.enums.Text
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.IdSource
import simulation.happenings.*
import simulation.main.Deck
import simulation.main.Hand
import simulation.main.handToDeck
import simulation.misc.ValueModifier

typealias AccessoryName = String

enum class AttachmentCategory {
  ability,
  animation,
  buff,
  equipped,
  inventory
}

data class Attachment(
    val target: Id,
    val category: AttachmentCategory,
    val index: Int = 0,
    val source: Id = 0L
)

data class Modifier(
    val type: ModifierId,
    val strength: Int
)

data class AccessoryDefinition(
    val name: Text,
    val modifiers: List<Modifier> = listOf()
)

data class ModifierDefinition(
    val name: Text,
    val type: ModifierType = ModifierType._notSpecified,
    val direction: ModifierDirection = ModifierDirection.none,
    val overTime: EventTrigger? = null,
    val valueModifier: ValueModifier? = null
)

fun getTargetAttachments(deck: Deck, target: Id) =
    deck.attachments
        .filter { attachment ->
      attachment.value.target == target
    }

fun getAttachmentOfEntityType(deck: Deck, target: Id, type: ModifierId): Id? =
    getTargetAttachments(deck, target)
        .keys.firstOrNull {
      val buff = deck.buffs[it]
      buff?.type == type
    }

fun getTargetAttachmentsOfCategory(deck: Deck, target: Id, category: AttachmentCategory): List<Id> =
    getTargetAttachments(deck, target)
        .filter { it.value.category == category }
        .mapNotNull { it.key }

fun updateAttachment(events: Events): (Id, Attachment) -> Attachment {
  val purchaseEvents = events.filterIsInstance<PurchaseEvent>()

  return { id, attachment ->
    val purchase = purchaseEvents.firstOrNull { it.ware == id }
    if (purchase != null)
      Attachment(
          category = AttachmentCategory.ability,
          target = purchase.customer
      )
    else
      attachment
  }
}

fun cleanupAttachmentSource(deck: Deck): (Attachment) -> Attachment = { attachment ->
  val source = if (attachment.source > 0 && deck.bodies.containsKey(attachment.source))
    attachment.source
  else
    0L
  attachment.copy(
      source = source
  )
}

fun applyBuff(deck: Deck, nextId: IdSource): (ApplyBuffEvent) -> Deck = { event ->
  val modifierType = event.buffType
  val duration = event.duration
  val existing = getAttachmentOfEntityType(deck, event.target, modifierType)
  if (existing != null)
    Deck(
        timers = mapOf(
            existing to Timer(
                duration = duration
            )
        )
    )
  else {
    val hand = Hand(
        attachment = Attachment(
            target = event.target,
            category = AttachmentCategory.buff,
            source = event.source
        ),
        buff = Modifier(
            type = modifierType,
            strength = event.strength
        ),
        timer = Timer(
            duration = duration
        )
    )
    handToDeck(nextId(), hand)
  }
}

fun applyBuffsFromEvents(deck: Deck, nextId: IdSource, events: Events): List<Deck> =
    events
        .filterIsInstance<ApplyBuffEvent>()
        .map(applyBuff(deck, nextId))
