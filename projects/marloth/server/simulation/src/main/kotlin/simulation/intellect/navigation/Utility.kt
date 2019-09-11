package simulation.intellect.navigation

import mythic.spatial.Vector3

fun asRecastVector3(value: Vector3) = floatArrayOf(value.x, value.z, value.y)
fun fromRecastVector3(value: FloatArray) = Vector3(value[0], value[2], value[1])
