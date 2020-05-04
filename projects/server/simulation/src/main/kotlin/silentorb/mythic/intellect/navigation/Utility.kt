package silentorb.mythic.intellect.navigation

import silentorb.mythic.spatial.Vector3

fun toRecastVector3(value: Vector3) = floatArrayOf(value.x, value.z, value.y)
fun fromRecastVector3(value: FloatArray) = Vector3(value[0], value[2], value[1])
