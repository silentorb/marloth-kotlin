package simulation.main

import mythic.ent.WithId
import mythic.ent.Table

fun <Key, Value> replace(table: Map<Key, Value>, key: Key, value: Value): Map<Key, Value> =
    table.mapValues { if (it.key == key) value else it.value }

fun <T : WithId> replace(list: Table<T>, value: T): Table<T> =
    list
        .filterKeys { it != value.id }
        .plus(Pair(value.id, value))
