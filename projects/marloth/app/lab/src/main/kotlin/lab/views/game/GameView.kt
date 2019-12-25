package lab.views.game

import lab.FixtureId
import silentorb.mythic.glowing.globalState
import silentorb.mythic.sculpting.ImmutableFace
import silentorb.mythic.sculpting.getVerticesCenter
import silentorb.mythic.spatial.*
import silentorb.mythic.lookinglass.GameScene
import silentorb.mythic.lookinglass.SceneRenderer
import silentorb.mythic.lookinglass.shading.LightingConfig
import silentorb.mythic.lookinglass.shading.ObjectShaderConfig
import simulation.main.World

enum class GameDisplayMode {
  normal,
  wireframe
}

data class GameViewDrawConfig(
    var normals: Boolean = false,
    var skeletons: Boolean = false,
    var world: Boolean = true,
    var aiTargets: Boolean = false,
    var gui: Boolean = true
)

data class GameViewConfig(
    var seed: Long = 1,
    var UseRandomSeed: Boolean = false,
    var worldLength: Float = 100f,
    var haveEnemies: Boolean = true,
    var roomCount: Int = 15,
    var playerGravity: Boolean = true,
    var displayMode: GameDisplayMode = GameDisplayMode.normal,
    var draw: GameViewDrawConfig = GameViewDrawConfig(),
    var logDroppedFrames: Boolean = false,
    var autoNewGame: Boolean = true,
    var drawPhysics: Boolean = false,
    val lighting: LightingConfig = LightingConfig(),
    val fixture: FixtureId = FixtureId.none
)

fun renderFaceNormals(renderer: SceneRenderer, length: Float, faces: List<ImmutableFace>) {
  globalState.lineThickness = 2f
  for (face in faces) {
    val faceCenter = getVerticesCenter(face.unorderedVertices)
    val straightVerticalHack = Vector3(0.00001f, 0f, 0f)
    val transform = Matrix.identity
        .translate(faceCenter)
        .rotateTowards(Vector3m(face.normal + straightVerticalHack), Vector3m(0f, 0f, 1f))
        .rotateY(-Pi * 0.5f)
        .scale(length)

    renderer.flat.activate(ObjectShaderConfig(transform = transform, color = Vector4(0f, 1f, 0f, 1f)))
//    drawMesh(renderer.meshes[MeshId.line]!!.primitives[0].mesh, DrawMethod.lines)
  }
}

data class GameViewRenderData(
    val scenes: List<GameScene>,
    val world: World,
    val config: GameViewConfig
)

//fun renderStandardScene(renderers: List<GameSceneRenderer>, data: GameViewRenderData) {
//  renderers.forEach {
//    it.render()
//  }
//}

fun renderWireframeWorldMesh(renderer: SceneRenderer) {
//  val worldMesh = renderer.renderer.worldMesh
//  if (worldMesh != null) {
//    renderer.effects.flat.activate(ObjectShaderConfig(transform = Matrix.identity, color = Vector4(1f)))
//    var index = 0
//    throw Error("No longer implemented.")
////    for (texture in worldMesh.textureIndex) {
////      worldMesh.mesh.drawElement(DrawMethod.lineLoop, index++)
////    }
//  }
}

//fun renderWireframeScene(renderers: List<GameSceneRenderer>, data: GameViewRenderData) {
//  renderers.forEach {
//    it.prepareRender(listOf())
//    renderWireframeWorldMesh(it.renderer)
//    it.renderElements()
//  }
//}
//
//fun renderWireframeScene(renderer: GameSceneRenderer) {
//  renderer.prepareRender(listOf())
//  renderWireframeWorldMesh(renderer.renderer)
//  renderer.renderElements()
//  renderer.finishRender(renderer.renderer.viewport.zw, listOf())
//}

//fun renderNormalScene(renderer: GameSceneRenderer, config: GameViewConfig) {
//  val r = renderer.renderer.renderer
//  val filters = getDisplayConfigFilters(r.config)
//  renderer.prepareRender(filters)
//  if (config.draw.world)
//    renderer.renderWorldMesh()
//
//  renderer.renderElements()
//
////  renderSkyBox(r.mappedTextures, r.meshes, r.shaders)
//  renderer.finishRender(renderer.renderer.viewport.zw)
//  globalState.cullFaces = false
//}

//fun renderLabScenes(client: Client, data: GameViewRenderData) {
//  val windowInfo = client.getWindowInfo()
//  val renderer = client.renderer
//  renderer.prepareRender(windowInfo)
//
//  val canvas = createCanvas(client.renderer, windowInfo)
//  val config = data.config
//
//  val viewports = getPlayerViewports(data.scenes.size, windowInfo.dimensions).iterator()
//  for (scene in data.scenes) {
//    val viewport = viewports.next()
//    val sceneRenderer = renderer.createSceneRenderer(scene.mainMenu, viewport)
//    val gameRenderer = GameSceneRenderer(scene, sceneRenderer)
//    when (data.config.displayMode) {
//      GameDisplayMode.normal -> renderScene(gameRenderer)
//      GameDisplayMode.wireframe -> renderWireframeScene(gameRenderer)
//    }
//
////    if (config.draw.normals)
////      renderFaceNormals(sceneRenderer, 1f, data.world.realm.mesh)
//
//    if (config.draw.skeletons) {
////      scene.elements.filter { it.depiction == DepictionType.child || it.depiction == DepictionType.monster }
////          .forEach {
////            val info = it.animation!!
////            val armature = info.armature
////            drawSkeleton(sceneRenderer, armature.bones, it.transform)
////          }
//    }
//
//    if (config.draw.gui)
//      renderGui(Bounds(viewport), canvas, data.world, data.menuState)
//  }
//
//  renderer.finishRender(windowInfo)
//}

//fun updateLabGameState(config: GameViewConfig, commands: List<HaftCommand<LabCommandType>>) {
//  if (isActive(commands, LabCommandType.toggleMeshDisplay)) {
//    config.displayMode = if (config.displayMode == GameDisplayMode.normal)
//      GameDisplayMode.wireframe
//    else
//      GameDisplayMode.normal
//  }
//}

//fun updateGameView(client: Client, world: World?, state: LabState): Pair<ClientState, UserCommands> {
//  val windowInfo = client.getWindowInfo()
//  val boxes = layoutGui(client, state.app.client, world, windowInfo)
//  renderMain(client, windowInfo, state.app, boxes)
//  val players = client.screens.map { it.playerId }
//  return updateClient(client, players, state.app.client, world, boxes)
//}
