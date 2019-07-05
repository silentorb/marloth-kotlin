package rendering

import mythic.spatial.Matrix
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import scenery.*

data class ElementAnimation(
    val animationId: AnimationId,
    val timeOffset: Float,
    val strength: Float
)

data class MeshElement(
    val id: Long,
    val mesh: MeshName,
    val transform: Matrix,
    val material: Material? = null
)

data class AttachedMesh(
    val socket: String,
    val mesh: MeshElement
)

data class TexturedBillboard(
    val texture: TextureName,
    val position: Vector3,
    val color: Vector4,
    val scale: Float,
    val step: Int = 0
)

data class ElementGroup(
    val meshes: List<MeshElement> = listOf(),
    val armature: ArmatureId? = null,
    val animations: List<ElementAnimation> = listOf(),
    val attachments: List<AttachedMesh> = listOf(),
    val billboards: List<TexturedBillboard> = listOf()
)

typealias ElementGroups = List<ElementGroup>
