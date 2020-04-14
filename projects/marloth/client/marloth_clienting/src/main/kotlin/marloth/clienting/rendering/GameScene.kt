package marloth.clienting.rendering

import silentorb.mythic.lookinglass.ElementGroups
import silentorb.mythic.lookinglass.SceneLayers
import silentorb.mythic.lookinglass.ScreenFilter
import silentorb.mythic.scenery.Camera
import silentorb.mythic.scenery.Light
import silentorb.mythic.scenery.Scene

data class GameScene(
    val main: Scene,
    val layers: SceneLayers,
    val filters: List<ScreenFilter>
) {

  val camera: Camera
    get() = main.camera

  val lights: List<Light>
    get() = main.lights
}
