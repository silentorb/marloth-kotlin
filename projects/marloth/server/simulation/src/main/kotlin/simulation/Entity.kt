package simulation

import mythic.ent.Entity
import mythic.ent.Table

enum class EntityType {
  character,
  furnishing,
  missile
}

fun <Key, Value> replace(table: Map<Key, Value>, key: Key, value: Value): Map<Key, Value> =
    table.mapValues { if (it.key == key) value else it.value }

fun <T : Entity> replace(list: Table<T>, value: T): Table<T> =
    list
        .filterKeys { it != value.id }
        .plus(Pair(value.id, value))
