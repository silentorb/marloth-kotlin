package lab.views.map

import lab.views.game.renderFaceNormals
import marloth.clienting.Client
import mythic.glowing.DrawMethod
import mythic.glowing.globalState
import mythic.spatial.*
import mythic.typography.TextStyle
import org.joml.Vector4i
import org.lwjgl.opengl.GL11
import rendering.*
import scenery.Camera
import scenery.ProjectionType
import scenery.Scene
import simulation.Realm

fun drawWireframeWorld(renderer: SceneRenderer, worldMesh: WorldMesh, realm: Realm, config: MapViewConfig, color: Vector4) {
  val faces = realm.nodeList.flatMap { it.faces }.distinct()
//      .take(1)
  for (face in faces) {
//      renderer.effects.flat.activate(ObjectShaderConfig(color = color))
//      renderer.drawOutlinedFace(face.vertices, color)
    val debugInfo = realm.faces[face]!!.debugInfo
    val c = when (debugInfo) {
      "lower" -> Vector4(1f, 0f, 1f, 1f)
      else -> color
    }
    val thickness = if (config.selection.contains(faces.indexOf(face)))
      3f
    else
      1f
    for (edge in realm.mesh.faces[face]!!.edges) {
      renderer.drawLine(edge.first, edge.second, c, thickness)
//                sector.mesh.drawElement(DrawMethod.lineLoop, index++)
    }
  }
}

fun renderFaceIds(renderer: SceneRenderer, realm: Realm) {
  globalState.depthEnabled = true
  val textStyle = TextStyle(renderer.renderer.fonts[0], 0f, Vector4(0.5f, 1f, 1f, 1f))
  for (node in realm.nodeList) {
    for (faceId in node.faces) {
      val face = realm.mesh.faces[faceId]!!
      renderer.drawText(face.id.toString(), getCenter(face.vertices), textStyle)
    }
  }
}

fun renderNodeIds(renderer: SceneRenderer, realm: Realm) {
  globalState.depthEnabled = true
  val textStyle = TextStyle(renderer.renderer.fonts[0], 0f, Vector4(0.5f, 1f, 1f, 1f))
  for (node in realm.nodeList) {
    renderer.drawText(node.id.toString(), node.position, textStyle)
  }
}

fun renderMapMesh(renderer: SceneRenderer, world: Realm, config: MapViewConfig) {
  val worldMesh = renderer.renderer.worldMesh
  if (worldMesh != null) {
    if (config.display.drawMode == MapViewDrawMode.wireframe) {
      drawWireframeWorld(renderer, worldMesh, world, config, Vector4(1f))
    } else {
      globalState.depthEnabled = false
      globalState.cullFaces = false
      globalState.blendEnabled = true
      globalState.blendFunction = Pair(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

      val lineHalfColor = Vector4(1f, 1f, 1f, 1f)
//      val lineHalfColor = Vector4(1f, 1f, 1f, 0.5f)
//      drawWireframeWorld(renderer, worldMesh, world, lineHalfColor)
      globalState.depthEnabled = true
      globalState.cullFaces = true
      for (sector in worldMesh.sectors) {
        var index = 0
        for (textureId in sector.textureIndex) {
          val texture = renderer.renderer.mappedTextures[textureId]
              ?: renderer.renderer.textures[textureId.toString()]!!
          renderer.effects.texturedFlat.activate(ObjectShaderConfig(texture = texture))
          sector.mesh.drawElement(DrawMethod.triangleFan, index++)
        }
      }
      globalState.depthEnabled = false
      globalState.cullFaces = false
      drawWireframeWorld(renderer, worldMesh, world, config, lineHalfColor)
    }

    if (config.display.normals)
      renderFaceNormals(renderer, 0.3f, world.mesh)

    if (config.display.faceIds) {
      renderFaceIds(renderer, world)
    }

    if (config.display.nodeIds) {
      renderNodeIds(renderer, world)
    }
  }

//  globalState.depthEnabled = true
//  renderer.drawLine(config.tempStart, config.tempEnd, yellow)
}

fun createTopDownCamera(camera: MapViewCamera): Camera {
  val position = Vector3().transform(Matrix()
      .translate(camera.target)
      .rotateZ(camera.yaw)
      .rotateX(-camera.pitch)
      .translate(Vector3(0f, -camera.distance * 2f, 0f))
//      .translate(camera.target)
//      .rotateZ(camera.yaw)
//      .translate(Vector3(0f, -camera.distance, camera.distance))
  )
  return Camera(
      ProjectionType.orthographic,
      position,
      Quaternion(),
//      45f,
      nearClip = 0.001f,
      angleOrZoom = camera.distance,

      lookAt = camera.target
  )
}

fun renderMapView(client: Client, world: Realm, config: MapViewConfig, camera: Camera) {
  val windowInfo = client.getWindowInfo()
  val renderer = client.renderer
  renderer.prepareRender(windowInfo)

  val canvas = createCanvas(client.renderer, windowInfo)
  val scene = Scene(
      camera = camera,
      lights = listOf()
  )
  val viewport = Vector4i(0, 0, windowInfo.dimensions.x, windowInfo.dimensions.y)
  val sceneRenderer = renderer.createSceneRenderer(scene, viewport)
  renderMapMesh(sceneRenderer, world, config)
  renderer.finishRender(windowInfo)
}