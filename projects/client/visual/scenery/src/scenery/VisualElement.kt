package scenery

import spatial.Matrix

enum class Depiction {
  child,
  test
}

data class VisualElement(
    val depiction: Depiction,
    val transform: Matrix
)