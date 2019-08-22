package simulation.misc

import mythic.ent.Table
import mythic.ent.WithId
import mythic.spatial.Pi2
import scenery.MeshName
import scenery.Shape
import scenery.enums.ResourceId

fun <Key, Value> replace(table: Map<Key, Value>, key: Key, value: Value): Map<Key, Value> =
    table.mapValues { if (it.key == key) value else it.value }

fun <T : WithId> replace(list: Table<T>, value: T): Table<T> =
    list
        .filterKeys { it != value.id }
        .plus(Pair(value.id, value))

fun <T> updateField(defaultValue: T, newValue: T?): T =
    if (newValue != null)
      newValue
    else
      defaultValue

fun simplifyRotation(value: Float): Float =
    if (value > Pi2)
      value % (Pi2)
    else if (value < -Pi2)
      -(Math.abs(value) % Pi2)
    else
      value
