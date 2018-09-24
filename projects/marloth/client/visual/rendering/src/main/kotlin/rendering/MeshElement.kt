package rendering

import mythic.spatial.Matrix
import scenery.AnimationId
import scenery.ArmatureId
import scenery.MeshId

data class ElementAnimation(
    val animationId: AnimationId,
    val timeOffset: Float,
    val strength: Float = 1f
)

data class MeshElement(
    val id: Long,
    val mesh: MeshId,
    val transform: Matrix
)

data class ElementGroup(
    val meshes: List<MeshElement>,
    val armature: ArmatureId? = null,
    val animations: List<ElementAnimation> = listOf()
)

typealias ElementGroups = List<ElementGroup>
