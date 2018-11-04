package simulation

import mythic.ent.Entity

enum class EntityType {
  character,
  furnishing,
  missile
}

fun <Key, Value> replace(table: Map<Key, Value>, key: Key, value: Value): Map<Key, Value> =
    table.mapValues { if (it.key == key) value else it.value }

fun <T : Entity> replace(list: List<T>, value: T): List<T> =
    list
        .filter { it.id != value.id }
        .plus(value)
