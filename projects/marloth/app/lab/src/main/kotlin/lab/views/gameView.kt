package lab.views

import haft.isActive
import lab.LabCommandType
import marloth.clienting.Client
import marloth.clienting.gui.MenuState
import marloth.clienting.gui.renderGui
import mythic.bloom.Bounds
import mythic.glowing.DrawMethod
import mythic.glowing.globalState
import mythic.sculpting.FlexibleMesh
import mythic.sculpting.getVerticesCenter
import mythic.spatial.*
import rendering.*
import scenery.GameScene
import simulation.AbstractWorld

enum class GameDisplayMode {
  normal,
  wireframe
}

data class GameViewConfig(
    var seed: Long = 1,
    var UseRandomSeed: Boolean = false,
    var worldLength: Float = 100f,
    var haveEnemies: Boolean = true,
    var displayMode: GameDisplayMode = GameDisplayMode.normal,
    var drawNormals: Boolean = false
)

fun renderFaceNormals(renderer: SceneRenderer, length: Float, mesh: FlexibleMesh) {
  globalState.lineThickness = 2f
  for (face in mesh.faces) {
    val faceCenter = getVerticesCenter(face.unorderedVertices)
    val transform = Matrix()
        .translate(faceCenter)
        .rotateTowards(face.normal, Vector3(0f, 0f, 1f))
        .rotateY(-Pi * 0.5f)
        .scale(length)

    renderer.effects.flat.activate(transform, Vector4(0f, 1f, 0f, 1f))
    renderer.meshes[MeshType.line]!![0].mesh.draw(DrawMethod.lines)
  }
}

data class GameViewRenderData(
    val scenes: List<GameScene>,
    val world: AbstractWorld,
    val config: GameViewConfig,
    val menuState: MenuState
)

fun renderStandardScene(renderers: List<GameSceneRenderer>, data: GameViewRenderData) {
  renderers.forEach {
    it.render()
  }
}

fun renderWireframeWorldMesh(renderer: SceneRenderer) {
  val worldMesh = renderer.renderer.worldMesh
  if (worldMesh != null) {
    renderer.effects.flat.activate(Matrix(), Vector4(1f))
    var index = 0
    throw Error("No longer implemented.")
//    for (texture in worldMesh.textureIndex) {
//      worldMesh.mesh.drawElement(DrawMethod.lineLoop, index++)
//    }
  }
}

fun renderWireframeScene(renderers: List<GameSceneRenderer>, data: GameViewRenderData) {
  renderers.forEach {
    it.prepareRender()
    renderWireframeWorldMesh(it.renderer)
    it.renderElements()
  }
}

fun renderWireframeScene(renderer: GameSceneRenderer, data: GameViewRenderData) {
  renderer.prepareRender()
  renderWireframeWorldMesh(renderer.renderer)
  renderer.renderElements()
}

fun renderScene(client: Client, data: GameViewRenderData) {
  val windowInfo = client.getWindowInfo()
  val renderer = client.renderer
  renderer.prepareRender(windowInfo)

  val canvas = createCanvas(client.renderer, windowInfo)

  val viewports = getPlayerViewports(data.scenes.size, windowInfo.dimensions).iterator()
  for (scene in data.scenes) {
    val viewport = viewports.next()
    val sceneRenderer = renderer.createSceneRenderer(scene.main, viewport)
    val gameRenderer = GameSceneRenderer(scene, sceneRenderer)
    when (data.config.displayMode) {
      GameDisplayMode.normal -> gameRenderer.render()
      GameDisplayMode.wireframe -> renderWireframeScene(gameRenderer, data)
    }

    if (data.config.drawNormals)
      renderFaceNormals(sceneRenderer, 1f, data.world.mesh)

    renderGui(sceneRenderer, Bounds(viewport.toVector4()), canvas, data.menuState)
  }
//  val renderers = mapGameSceneRenderers(renderer, data.scenes, windowInfo)

  renderer.finishRender(windowInfo)
}

class GameView(val config: GameViewConfig) {
//   fun createLayout(dimensions: Vector2i): List<Box> {
//    return listOf()
//  }

  fun updateState(input: InputState, delta: Float) {
    val commands = input.commands

    if (isActive(commands, LabCommandType.toggleMeshDisplay)) {
      config.displayMode = if (config.displayMode == GameDisplayMode.normal)
        GameDisplayMode.wireframe
      else
        GameDisplayMode.normal
    }
  }

//   fun getCommands(): LabCommandMap = mapOf()
}
