package simulation.entities

enum class ViewMode {
  firstPerson,
  //  topDown
  thirdPerson
}

data class HoverCamera(
    var pitch: Float = -0.4f,
    var yaw: Float = 0f,
    var distance: Float = 7f
)

data class Player(
    val name: String,
    val hoverCamera: HoverCamera = HoverCamera()
)
