package scenery

enum class CameraMode {
  firstPerson,
  topDown,
  thirdPerson
}

data class Screen(
//    var cameraMode: CameraMode,
    val playerId: Int
)

data class Scene(
    val camera: Camera,
    val lights: List<Light> = listOf()
)

enum class Gender {
  female,
  male
}

data class ChildDetails(
    val gender: Gender
)

data class ElementDetails(
    val children: Map<Id, ChildDetails>
)

data class GameScene(
    val main: Scene,
    val elements: List<VisualElement>,
    val elementDetails: ElementDetails,
    val player: Int
) {

  val camera: Camera
    get() = main.camera

  val lights: List<Light>
    get() = main.lights
}