package rendering

import mythic.spatial.Matrix
import scenery.AnimationId
import scenery.ArmatureId
import scenery.MeshId

data class ElementAnimation(
    val animationId: AnimationId,
    val timeOffset: Float,
    val strength: Float
)

data class MeshElement(
    val id: Long,
    val mesh: MeshId,
    val transform: Matrix,
    val material: Material? = null
)

data class AttachedMesh(
    val socket: String,
    val mesh: MeshElement
)

data class ElementGroup(
    val meshes: List<MeshElement>,
    val armature: ArmatureId? = null,
    val animations: List<ElementAnimation> = listOf(),
    val attachments: List<AttachedMesh> = listOf()
)

typealias ElementGroups = List<ElementGroup>
