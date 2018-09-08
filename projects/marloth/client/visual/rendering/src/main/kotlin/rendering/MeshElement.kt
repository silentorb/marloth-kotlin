package rendering

import mythic.breeze.Armature
import mythic.spatial.Matrix

data class DepictionAnimation(
    val animationIndex: Int,
    var timeOffset: Float,
    val armature: Armature
)

data class MeshElement(
    val id: Long,
    val mesh: MeshType,
    val animation: DepictionAnimation? = null,
    val transform: Matrix
)
