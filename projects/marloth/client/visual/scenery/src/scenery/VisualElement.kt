package scenery

import mythic.spatial.Matrix

enum class Depiction {
  child,
  test,
  world
}

data class VisualElement(
    val depiction: Depiction,
    val transform: Matrix
)