package scenery

import mythic.spatial.Matrix

data class VisualElement(
    val depiction: DepictionType,
    val transform: Matrix
)