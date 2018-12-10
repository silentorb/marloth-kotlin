package lab.views.map

import lab.utility.embedCameraView
import lab.views.game.renderFaceNormals
import marloth.clienting.Client
import mythic.bloom.*
import mythic.glowing.DrawMethod
import mythic.glowing.globalState
import mythic.spatial.*
import mythic.typography.IndexedTextStyle
import mythic.typography.TextStyle
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
  val faces = nodes.flatMap { it.faces }.distinct()
//      .take(1)
  for (face in faces) {
//      renderer.effects.flat.activate(ObjectShaderConfig(color = color))
//      renderer.drawOutlinedFace(face.vertices, color)
    val debugInfo = realm.faces[face]!!.debugInfo
    val c = when (debugInfo) {
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

private val textStyle = IndexedTextStyle(0, 0f, Vector4(0.5f, 1f, 1f, 1f))

fun renderFaceIds(renderer: SceneRenderer, realm: Realm, nodes: Collection<Node>) {
  globalState.depthEnabled = true
  for (faceId in nodes.flatMap { it.faces }) {
//    for (faceId in node.faces) {
    val face = realm.mesh.faces[faceId]!!
    renderer.drawText(face.id.toString(), getCenter(face.vertices), textStyle)
//    }
  }
}

fun renderNodeIds(renderer: SceneRenderer, nodes: Collection<Node>) {
  globalState.depthEnabled = true
  for (node in nodes) {
    renderer.drawText(node.id.toString(), node.position, textStyle)
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

  val faces = nodes.flatMap { it.faces }.map { realm.mesh.faces[it]!! }

  if (config.display.drawMode == MapViewDrawMode.wireframe) {
    drawWireframeWorld(renderer, worldMesh, realm, config, nodes, Vector4(1f))
  } else {
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
            ?: renderer.renderer.textures[textureId.toString()]!!
        renderer.effects.texturedFlat.activate(ObjectShaderConfig(texture = texture))
        sector.mesh.drawElement(DrawMethod.triangleFan, index++)
      }
    }
    globalState.depthEnabled = false
    globalState.cullFaces = false
    drawWireframeWorld(renderer, worldMesh, realm, config, nodes, lineHalfColor)
  }

  if (config.display.normals)
    renderFaceNormals(renderer, 0.5f, faces)

  if (config.display.faceIds) {
    renderFaceIds(renderer, realm, nodes)
  }

  if (config.display.nodeIds) {
    renderNodeIds(renderer, nodes)
  }

  for (id in config.selection) {
    val face = realm.mesh.faces[id]!!
    face.vertices.forEachIndexed { index, v ->
      renderer.drawText(index.toString(), v, textStyle)
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