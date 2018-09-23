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


data class BillboardDetails(
    val text: String,
    val cooldown: Float? = null
)
