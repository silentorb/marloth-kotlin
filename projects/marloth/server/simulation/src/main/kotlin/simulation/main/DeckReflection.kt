package simulation.main

import silentorb.mythic.ent.Id
import silentorb.mythic.ent.IdSource
import silentorb.mythic.ent.Table
import simulation.entities.Attachment
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName

private val _deckType = Deck::class
private val _deckConstructor = _deckType.constructors.first()
private val _deckProperties = _deckConstructor.parameters
    .map { p -> _deckType.memberProperties.first { it.name == p.name } }

private val _handType = Hand::class
private val _handProperties = _deckConstructor.parameters
    .map { p ->
      _handType.memberProperties.first {
        it.returnType.jvmErasure.jvmName == p.type.arguments[1].type!!.jvmErasure.jvmName
      }
    }

private fun _newDeck(args: List<Table<Any>>): Deck =
    _deckConstructor.call(*args.toTypedArray())

fun removeEntities(deck: Deck, removeIds: Set<Id>): Deck {
  val isActive = { id: Id -> !removeIds.contains(id) }
  val deletions = _deckProperties.map { property ->
    val value = property.get(deck) as Table<Any>
    value.filterKeys(isActive)
  }
  return _newDeck(deletions)
}

fun Deck.plus(other: Deck): Deck {
  val additions = _deckProperties.map { property ->
    val first = property.get(this) as Table<Any>
    val second = property.get(other) as Table<Any>
    first.plus(second)
  }
  return _newDeck(additions)
}

fun toDeck(id: Id, hand: Hand): Deck {
  assert(hand.attachments.isEmpty())

  val additions = _handProperties.map { property ->
    val value = property.get(hand) as Any?
    nullableList(id, value)
  }
  return _newDeck(additions)
}

fun toDeck(nextId: IdSource, hand: Hand): Deck {
  val id = nextId()
  val additions = _handProperties.map { property ->
    val value = property.get(hand) as Any?
    nullableList(id, value)
  }
  val deck = _newDeck(additions)
  return if (hand.attachments.any()) {
    val hands = hand.attachments.map {
      it.hand.copy(
          attachment = Attachment(
              target = id,
              category = it.category,
              index = it.index
          )
      )
    }
    deck.plus(allHandsOnDeck(hands, nextId))
  } else
    deck
}
