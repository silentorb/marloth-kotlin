package lab.views.map

import marloth.clienting.Client
import mythic.glowing.DrawMethod
import mythic.glowing.globalState
import mythic.spatial.*
import org.joml.Vector4i
import org.joml.plus
import org.joml.times
import org.lwjgl.opengl.GL11
import rendering.*
import scenery.Camera
import scenery.ProjectionType
import scenery.Scene
import simulation.AbstractWorld

fun drawWireframeWorld(renderer: SceneRenderer, worldMesh: WorldMesh, color: Vector4) {
  renderer.effects.flat.activate(ObjectShaderConfig(color = color))
  for (sector in worldMesh.sectors) {
    var index = 0
    for (texture in sector.textureIndex) {
      sector.mesh.drawElement(DrawMethod.lineLoop, index++)
    }
  }
}

fun renderMapMesh(renderer: SceneRenderer, config: MapViewDisplayConfig) {
  val worldMesh = renderer.renderer.worldMesh
  if (worldMesh != null) {
    if (config.drawMode == MapViewDrawMode.wireframe) {
      drawWireframeWorld(renderer, worldMesh, Vector4(1f))
    } else {
      globalState.depthEnabled = false
      globalState.cullFaces = false
      globalState.blendEnabled = true
      globalState.blendFunction = Pair(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

      val lineHalfColor = Vector4(1f, 1f, 1f, 0.5f)
      drawWireframeWorld(renderer, worldMesh, lineHalfColor)
      globalState.depthEnabled = true
      globalState.cullFaces = true
      for (sector in worldMesh.sectors) {
        var index = 0
        for (texture in sector.textureIndex) {
          renderer.effects.texturedFlat.activate(ObjectShaderConfig(texture = texture))
          sector.mesh.drawElement(DrawMethod.triangleFan, index++)
        }
      }
      globalState.depthEnabled = false
      globalState.cullFaces = false
      drawWireframeWorld(renderer, worldMesh, lineHalfColor)
    }
  }
}

fun createTopDownCamera(camera: MapViewCamera): Camera {
  val position = Vector3(0f, -camera.distance, camera.distance) + camera.target
  return Camera(
      ProjectionType.perspective,
      position,
      Quaternion().rotateZ(Pi * 0.5f)
          *
          Quaternion().rotateY(Pi * 0.25f)
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
  renderMapMesh(sceneRenderer, config.display)
  renderer.finishRender(windowInfo)
}