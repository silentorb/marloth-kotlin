package simulation.main

import mythic.ent.Id
import mythic.ent.Table
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName

private val _deckType = Deck::class
//private val _deckCopyFunction = _deckType.memberFunctions.first { it.name == "copy" }
private val _deckConstructor = _deckType.constructors.first()
private val _deckProperties = _deckConstructor.parameters
    .map { p -> _deckType.memberProperties.first { it.name == p.name } }

private val _handType = Hand::class
private val _handProperties = _deckConstructor.parameters
    .map { p -> _handType.memberProperties.first {
      it.returnType.jvmErasure.jvmName == p.type.arguments[1].type!!.jvmErasure.jvmName }
    }

private fun _newDeck(args: List<Table<Any>>): Deck =
    _deckConstructor.call(*args.toTypedArray())

fun removeEntities(deck: Deck, removeIds: List<Id>): Deck {
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
  val additions = _handProperties.map { property ->
    val value = property.get(hand) as Any?
    nullableList(id, value)
  }
  return _newDeck(additions)
}
