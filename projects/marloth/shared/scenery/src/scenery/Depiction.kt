package scenery

import mythic.breeze.Armature

typealias Id = Int

data class DepictionAnimation(
    val animationIndex: Int,
    var timeOffset: Float,
    val armature: Armature
)

data class Depiction(
    val id: Id,
    val type: DepictionType,
    val animation: DepictionAnimation?
)