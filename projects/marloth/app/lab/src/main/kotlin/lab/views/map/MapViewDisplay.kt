package lab.views.map

import lab.views.GameDisplayMode
import lab.views.GameViewRenderData
import lab.views.renderFaceNormals
import lab.views.renderWireframeScene
import marloth.clienting.Client
import marloth.clienting.gui.renderGui
import mythic.bloom.Bounds
import mythic.glowing.DrawMethod
import mythic.spatial.*
import org.joml.Vector4i
import org.joml.plus
import org.joml.times
import physics.Body
import rendering.*
import scenery.Camera
import scenery.ProjectionType
import scenery.Scene
import simulation.AbstractWorld

fun renderWireframeWorldMesh(renderer: SceneRenderer) {
  val worldMesh = renderer.renderer.worldMesh
  if (worldMesh != null) {
    renderer.effects.flat.activate(ObjectShaderConfig(transform = Matrix(), color = Vector4(1f)))
    for (sector in worldMesh.sectors) {
      var index = 0
      for (texture in sector.textureIndex) {
        sector.mesh.drawElement(DrawMethod.lineLoop, index++)
      }
    }
  }
}

fun createTopDownCamera(camera: MapViewCamera): Camera {
  val position = Vector3(0f, -camera.distance, camera.distance) + camera.target
  return Camera(
      ProjectionType.perspective,
      position,
      Quaternion().rotate(0f, 0f, Pi * 0.5f)
          *
          Quaternion().rotate(0f, Pi * 0.25f, 0f)
      ,
      45f
  )
}

fun renderMapView(client: Client, world: AbstractWorld, config: MapViewConfig) {
  val windowInfo = client.getWindowInfo()
  val renderer = client.renderer
  renderer.prepareRender(windowInfo)

  val canvas = createCanvas(client.renderer, windowInfo)
  val scene = Scene(
      camera = createTopDownCamera(config.camera),
      lights = listOf()
  )
  val viewport = Vector4i(0, 0, windowInfo.dimensions.x, windowInfo.dimensions.y)
  val sceneRenderer = renderer.createSceneRenderer(scene, viewport)
  renderWireframeWorldMesh(sceneRenderer)
//  val viewports = getPlayerViewports(data.scenes.size, windowInfo.dimensions).iterator()
//  for (scene in data.scenes) {
//    val viewport = viewports.next()
//    val sceneRenderer = renderer.createSceneRenderer(scene.main, viewport)
//    val gameRenderer = GameSceneRenderer(scene, sceneRenderer)
//    when (data.config.displayMode) {
//      GameDisplayMode.normal -> gameRenderer.render()
//      GameDisplayMode.wireframe -> renderWireframeScene(gameRenderer, data)
//    }
//
//    if (data.config.drawNormals)
//      renderFaceNormals(sceneRenderer, 1f, data.world.mesh)
//
//    renderGui(sceneRenderer, Bounds(viewport.toVector4()), canvas, data.menuState)
//  }

  renderer.finishRender(windowInfo)
}