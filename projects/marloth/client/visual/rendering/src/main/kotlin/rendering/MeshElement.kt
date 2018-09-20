package rendering

import mythic.spatial.Matrix

data class ElementAnimation(
    val animation: Int,
    var timeOffset: Float,
    val strength: Float = 1f
)

data class MeshElement(
    val id: Long,
    val mesh: MeshType,
    val animations: List<ElementAnimation> = listOf(),
    val transform: Matrix
)

data class ElementGroup(
    val meshes: List<MeshElement>
)

typealias ElementGroups = List<ElementGroup>
