package lab.views.map

import lab.utility.blue
import lab.utility.embedCameraView
import lab.utility.green
import lab.utility.red
import lab.views.game.renderFaceNormals
import lab.views.shared.LabTextStyles
import marloth.clienting.Client
import mythic.bloom.StateBag
import mythic.bloom.StateDepiction
import mythic.bloom.StateDepictionOld
import mythic.bloom.selectionState
import mythic.glowing.DrawMethod
import mythic.glowing.globalState
import mythic.glowing.unbindTexture
import mythic.spatial.*
import org.lwjgl.opengl.GL11
import rendering.*
import rendering.shading.ObjectShaderConfig
import scenery.Camera
import scenery.ProjectionType
import scenery.Scene
import simulation.Node
import simulation.Realm
import simulation.nodeNeighbors

fun drawWireframeWorld(renderer: SceneRenderer, worldMesh: WorldMesh, realm: Realm, config: MapViewConfig, nodes: Collection<Node>, color: Vector4) {
  val faces = if (config.display.isolateSelection && config.selection.any())
    config.selection
  else
    nodes.flatMap { it.faces }.distinct()
//      .take(1)

  for (face in faces) {
//      renderer.effects.flat.activate(ObjectShaderConfig(color = color))
//      renderer.drawOutlinedFace(face.vertices, color)
    val debugInfo = realm.faces[face]!!.debugInfo
    val c = when (debugInfo) {
      "incomplete" -> Vector4(1f, 1f, 0f, 1f)
      "lower" -> Vector4(1f, 0f, 1f, 1f)
      else -> color
    }
    val thickness = if (config.selection.contains(face))
      3f
    else
      1f
    for (edge in realm.mesh.faces[face]!!.edges) {
      renderer.drawLine(edge.first, edge.second, c, thickness)
//                sector.mesh.drawElement(DrawMethod.lineLoop, index++)
    }
  }
}

fun renderFaceIds(renderer: SceneRenderer, realm: Realm, nodes: Collection<Node>) {
  globalState.depthEnabled = true
  for (faceId in nodes.flatMap { it.faces }) {
//    for (faceId in node.faces) {
    val face = realm.mesh.faces[faceId]!!
    renderer.drawText(face.id.toString(), getCenter(face.vertices), LabTextStyles.lessRed)
//    }
  }
}

fun renderNodeIds(renderer: SceneRenderer, nodes: Collection<Node>) {
  globalState.depthEnabled = false
  for (node in nodes) {
    renderer.drawText(node.id.toString(), node.position, LabTextStyles.lessRed)
  }
}

fun renderMapMesh(renderer: SceneRenderer, realm: Realm, config: MapViewConfig, bag: StateBag) {
  val worldMesh = renderer.renderer.worldMesh!!
  val selectedNodes = selectionState(bag[nodeListSelectionKey])
  val nodes: Collection<Node> = if (selectedNodes.selection.none())
    realm.nodeList
  else
    selectedNodes.selection.map { it.toLong() }
        .flatMap { id ->
          nodeNeighbors(realm, id)
              .plus(id)
        }
        .distinct()
        .map { realm.nodeTable[it]!! }

  val sectors = if (selectedNodes.selection.none())
    worldMesh.sectors
  else
    worldMesh.sectors.filter { selectedNodes.selection.contains(it.id.toString()) }

  val faces = (if (config.display.isolateSelection && config.selection.any())
    config.selection
  else
    nodes.flatMap { it.faces })
      .map { realm.mesh.faces[it]!! }

  if (config.display.wireframe && !config.display.solid) {
    drawWireframeWorld(renderer, worldMesh, realm, config, nodes, Vector4(1f))
  } else if (config.display.solid) {
    globalState.depthEnabled = false
    globalState.cullFaces = false
    globalState.blendEnabled = true
    globalState.blendFunction = Pair(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

    val lineHalfColor = Vector4(1f, 1f, 1f, 1f)
//      val lineHalfColor = Vector4(1f, 1f, 1f, 0.5f)
//      drawWireframeWorld(renderer, worldMesh, realm, lineHalfColor)
    globalState.depthEnabled = true
    globalState.cullFaces = true

    for (sector in sectors) {
      var index = 0
      for (textureId in sector.textureIndex) {
        val texture = renderer.renderer.mappedTextures[textureId]
            ?: renderer.renderer.textures[textureId.toString()]

        if (texture != null)
          renderer.effects.texturedFlat.activate(ObjectShaderConfig(texture = texture))
        else
          unbindTexture()

        sector.mesh.drawElement(DrawMethod.triangleFan, index++)
      }
    }
    globalState.depthEnabled = false
    globalState.cullFaces = false

    if (config.display.wireframe)
      drawWireframeWorld(renderer, worldMesh, realm, config, nodes, lineHalfColor)
  }

  if (config.display.abstract) {
    drawAbstractNodes(renderer, nodes)
  }

  globalState.depthEnabled = false

  if (config.display.normals)
    renderFaceNormals(renderer, 0.5f, faces)

  if (config.display.faceIds) {
    renderFaceIds(renderer, realm, nodes)
  }

  if (config.display.nodeIds) {
    renderNodeIds(renderer, nodes)
  }

//  renderer.drawPoint(Vector3(), Vector4(1f, 0f, 0f, 1f), 3f)
  renderer.drawLine(Vector3(), Vector3(1f, 0f, 0f), red)
  renderer.drawLine(Vector3(), Vector3(0f, 1f, 0f), green)
  renderer.drawLine(Vector3(), Vector3(0f, 0f, 1f), blue)
//  renderer.drawPoint(Vector3(4.253298f, 64.679794f, 0.0f), Vector4(1f, 1f, 0f, 1f), 3f)
//  renderer.drawPoint(Vector3(3.3128958f, 39.977787f, 0.0f), Vector4(1f, 0f, 1f, 1f), 3f)
//  renderer.drawPoint(Vector3(16.027586f, 69.140396f, 0.0f), Vector4(0f, 1f, 1f, 1f), 3f)

  for (id in config.selection) {
    val face = realm.mesh.faces[id]
    if (face != null) {
      face.vertices.forEachIndexed { index, v ->
        renderer.drawText(index.toString(), v, LabTextStyles.lessRed)
      }
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
      .translate(Vector3(0f, -camera.distance * 4f, 0f))
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

fun renderMapView(client: Client, realm: Realm, config: MapViewConfig): StateDepiction = { seed ->
  embedCameraView { b, c ->
    val camera = createTopDownCamera(config.camera)
    val windowInfo = client.getWindowInfo()
    val renderer = client.renderer
    renderer.prepareRender(windowInfo)

    val scene = Scene(
        camera = camera,
        lights = listOf()
    )
    val sceneRenderer = renderer.createSceneRenderer(scene, b.toVector4i())
    renderMapMesh(sceneRenderer, realm, config, seed.bag)
    renderer.finishRender(windowInfo)
  }
}
