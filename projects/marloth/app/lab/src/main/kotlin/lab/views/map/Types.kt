package lab.views.map

import silentorb.mythic.ent.Id
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Vector3

enum class MapViewCameraMode {
  firstPerson,
  orbital
}

data class MapViewOrbitalCamera(
    var target: Vector3 = Vector3(),
    var distance: Float = 20f,
    var yaw: Float = 0f,
    var pitch: Float = Pi / 4f
)

data class MapViewFirstPersonCamera(
    var position: Vector3 = Vector3(),
    var yaw: Float = 0f,
    var pitch: Float = Pi / 4f
)

data class MapViewDisplayConfig(
    var solid: Boolean = true,
    var wireframe: Boolean = true,
    var normals: Boolean = false,
    var faceIds: Boolean = false,
    var nodeIds: Boolean = false,
    var isolateSelection: Boolean = false,
    var abstract: Boolean = false,
    var navMesh: Boolean = false,
    var navMeshInput: Boolean = false,
    var navMeshVoxels: Boolean = false
)

data class MapViewConfig(
    var cameraMode: MapViewCameraMode = MapViewCameraMode.orbital,
    val orbitalCamera: MapViewOrbitalCamera = MapViewOrbitalCamera(),
    val firstPersonCamera: MapViewFirstPersonCamera = MapViewFirstPersonCamera(),
    val display: MapViewDisplayConfig = MapViewDisplayConfig(),
    var selection: List<Id> = listOf(),
    var tempStart: Vector3 = Vector3(),
    var tempEnd: Vector3 = Vector3(),
    var raySkip: Int = 0
)
