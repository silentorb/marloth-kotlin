package scenery

import mythic.spatial.Matrix

data class VisualElement(
    val id: Id,
    val depiction: DepictionType,
    val transform: Matrix
)