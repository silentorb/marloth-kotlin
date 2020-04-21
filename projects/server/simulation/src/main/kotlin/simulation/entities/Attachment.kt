package simulation.entities

import marloth.scenery.enums.ModifierId
import silentorb.mythic.accessorize.Modifier
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.IdSource
import silentorb.mythic.happenings.Events
import silentorb.mythic.timing.IntTimer
import simulation.happenings.*
import simulation.main.Deck
import simulation.main.Hand
import simulation.main.handToDeck

enum class AttachmentCategory {
  ability,
  animation,
  equipped,
  inventory
}

data class Attachment(
    val target: Id,
    val category: AttachmentCategory,
    val index: Int = 0,
    val source: Id = 0L
)

fun getTargetAttachments(deck: Deck, target: Id) =
    deck.attachments
        .filter { attachment ->
          attachment.value.target == target
        }

fun getAttachmentOfEntityType(deck: Deck, target: Id, type: ModifierId): Id? =
    getTargetAttachments(deck, target)
        .keys.firstOrNull {
      val buff = deck.modifiers[it]
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
        timersInt = mapOf(
            existing to IntTimer(
                duration = duration,
                interval = 2
            )
        )
    )
  else {
    val hand = Hand(
        buff = Modifier(
            type = modifierType,
            strength = event.strength,
            target = event.target,
            source = event.source
        ),
        timer = IntTimer(
            duration = duration,
            interval = 2
        )
    )
    handToDeck(nextId(), hand)
  }
}

fun applyBuffsFromEvents(deck: Deck, nextId: IdSource, events: Events): List<Deck> =
    events
        .filterIsInstance<ApplyBuffEvent>()
        .map(applyBuff(deck, nextId))