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

//enum class Gender {
//  female,
//  male
//}
//
//data class ChildDetails(
//    val gender: Gender
//)

data class BillboardDetails(
    val text: String,
    val cooldown: Float? = null
)

//data class ElementDetails(
//    val children: Map<Long, ChildDetails>
////    val billboards: Map<Id, BillboardDetails>
//)
