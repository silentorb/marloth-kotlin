package rendering

import scenery.Camera
import scenery.Light
import scenery.Scene

data class GameScene(
    val main: Scene,
    val opaqueElementGroups: ElementGroups,
    val transparentElementGroups: ElementGroups,
    val player: Int,
    val filters: List<ScreenFilter>
) {

  val camera: Camera
    get() = main.camera

  val lights: List<Light>
    get() = main.lights
}
