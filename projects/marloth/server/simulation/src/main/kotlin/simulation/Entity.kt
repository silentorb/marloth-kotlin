package simulation

import mythic.ent.Entity
import mythic.ent.Id
import mythic.ent.IdSource

enum class EntityType {
  character,
  furnishing,
  missile
}

fun newIdSource(initialValue: Id): IdSource {
  var nextId: Id = initialValue
  return { nextId++ }
}

fun <Key, Value> replace(table: Map<Key, Value>, key: Key, value: Value): Map<Key, Value> =
    table.mapValues { if (it.key == key) value else it.value }

fun <T : Entity> replace(list: List<T>, value: T): List<T> =
    list
        .filter { it.id != value.id }
        .plus(value)
