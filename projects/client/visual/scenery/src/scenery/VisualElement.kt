package scenery

import spatial.Vector3

enum class Depiction {
  child,
  test
}

data class VisualElement(
    val depiction: Depiction,
    val position: Vector3
)