package marloth.integration.scenery

import marloth.clienting.rendering.GameScene
import silentorb.mythic.editing.Editor
import silentorb.mythic.lookinglass.ModelMeshMap
import silentorb.mythic.lookinglass.SceneLayer
import silentorb.mythic.scenery.Scene
import simulation.misc.Definitions

fun sceneFromEditorGraph(meshes: ModelMeshMap, definitions: Definitions, editor: Editor): GameScene {
  val camera = newFlyThroughCamera { defaultFlyThroughState() }
  val layers = listOf(
      SceneLayer(
          elements = listOf(),
          useDepth = false
      ),
  )
  return GameScene(
      main = Scene(
          camera = camera,
          lights = listOf(),
          lightingConfig = defaultLightingConfig()
      ),
      layers = layers,
      filters = listOf()
  )
}
