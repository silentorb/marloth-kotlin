package scenery

enum class CameraMode {
  firstPerson,
  topDown,
  thirdPerson
}

data class Screen(
    var cameraMode: CameraMode,
    val playerId: Int
)

data class Scene(
    val camera: Camera,
    val elements: List<VisualElement>,
    val player: Int
)