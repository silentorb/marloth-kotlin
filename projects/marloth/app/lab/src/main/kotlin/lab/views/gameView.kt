package lab.views

import haft.isActive
import lab.LabCommandType
import marloth.clienting.Client
import mythic.glowing.DrawMethod
import mythic.glowing.globalState
import mythic.platforming.WindowInfo
import mythic.sculpting.FlexibleMesh
import mythic.sculpting.getVerticesCenter
import mythic.spatial.Matrix
import mythic.spatial.Pi
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import org.joml.Vector2i
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

fun renderFaceNormals(renderer: SceneRenderer, mesh: FlexibleMesh) {
  globalState.lineThickness = 2f
  for (face in mesh.faces) {
    val faceCenter = getVerticesCenter(face.unorderedVertices)
    val transform = Matrix()
        .translate(faceCenter)
        .rotateTowards(face.normal, Vector3(0f, 0f, 1f))
        .rotateY(-Pi * 0.5f)

    renderer.effects.flat.activate(transform, Vector4(0f, 1f, 0f, 1f))
    renderer.meshes[MeshType.line]!![0].mesh.draw(DrawMethod.lines)
  }
}

data class GameViewRenderData(
    val scenes: List<GameScene>,
    val world: AbstractWorld,
    val config: GameViewConfig
)

fun renderNormalScene(renderers: List<GameSceneRenderer>, data: GameViewRenderData) {
  renderers.forEach {
    it.render()
  }
}

fun renderWireframeWorldMesh(renderer: SceneRenderer) {
  val worldMesh = renderer.renderer.worldMesh
  if (worldMesh != null) {
    renderer.effects.flat.activate(Matrix(), Vector4(1f))
    var index = 0
    for (texture in worldMesh.textureIndex) {
      worldMesh.mesh.drawElement(DrawMethod.lineLoop, index++)
    }
  }
}

fun renderWireframeScene(renderers: List<GameSceneRenderer>, data: GameViewRenderData) {
  renderers.forEach {
    it.prepareRender()
    renderWireframeWorldMesh(it.renderer)
    it.renderElements()
  }
}

fun renderScene(client: Client, data: GameViewRenderData) {
  val windowInfo = client.getWindowInfo()
  val renderer = client.renderer
  renderer.prepareRender(windowInfo)
  val renderers = mapGameSceneRenderers(renderer, data.scenes, windowInfo)
  when (data.config.displayMode) {
    GameDisplayMode.normal -> renderNormalScene(renderers, data)
    GameDisplayMode.wireframe -> renderWireframeScene(renderers, data)
  }
  renderers.forEach {
    if (data.config.drawNormals)
      renderFaceNormals(it.renderer, data.world.mesh)
  }
  renderer.finishRender(windowInfo)
}

class GameView(val config: GameViewConfig) : View {
  override fun createLayout(dimensions: Vector2i): LabLayout {
    return LabLayout(listOf())
  }

  override fun updateState(layout: LabLayout, input: InputState, delta: Float) {
    val commands = input.commands

    if (isActive(commands, LabCommandType.toggleMeshDisplay)) {
      config.displayMode = if (config.displayMode == GameDisplayMode.normal)
        GameDisplayMode.wireframe
      else
        GameDisplayMode.normal
    }
  }

  override fun getCommands(): LabCommandMap = mapOf()
}
