package lab.views.map

import lab.renderNavMesh
import lab.utility.blue
import lab.utility.embedCameraView
import lab.utility.green
import lab.utility.red
import lab.views.game.conditionalDrawAiTargets
import lab.views.shared.LabTextStyles
import marloth.clienting.Client
import marloth.scenery.creation.convertSimpleDepiction
import marloth.scenery.creation.defaultLightingConfig
import silentorb.mythic.bloom.StateBag
import silentorb.mythic.bloom.StateDepiction
import silentorb.mythic.bloom.selectionStateOld
import silentorb.mythic.glowing.globalState
import silentorb.mythic.lookinglass.SceneRenderer
import silentorb.mythic.lookinglass.createSceneRenderer
import silentorb.mythic.lookinglass.drawing.renderMeshElement
import silentorb.mythic.lookinglass.finishRender
import silentorb.mythic.lookinglass.prepareRender
import silentorb.mythic.scenery.Camera
import silentorb.mythic.scenery.ProjectionType
import silentorb.mythic.scenery.Scene
import silentorb.mythic.spatial.Matrix
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector4
import simulation.entities.DepictionType
import simulation.main.Deck
import simulation.main.World
import simulation.misc.Node
import simulation.misc.Realm
import simulation.misc.nodeNeighbors

fun drawWireframeWorld(renderer: SceneRenderer, deck: Deck, realm: Realm, config: MapViewConfig, nodes: Collection<Node>, color: Vector4) {

}

fun renderNodeIds(renderer: SceneRenderer, nodes: Collection<Node>) {
  globalState.depthEnabled = false
  for (node in nodes) {
    renderer.drawText(node.id.toString(), node.position, LabTextStyles.lessRed)
  }
}

fun renderMapMesh(sceneRenderer: SceneRenderer, realm: Realm, deck: Deck, config: MapViewConfig, bag: StateBag) {
//  val worldMesh = sceneRenderer.sceneRenderer.worldMesh!!
  val selectedNodes = selectionStateOld(bag[nodeListSelectionKey])
  val nodes: Collection<Node> = if (selectedNodes.none())
    realm.nodeList
  else
    selectedNodes.map { it.toLong() }
        .flatMap { id ->
          nodeNeighbors(realm.graph, id)
              .plus(id)
        }
        .distinct()
        .map { realm.nodeTable[it]!! }

//  val sectors = if (selectedNodes.selection.none())
//    worldMesh.sectors
//  else
//    worldMesh.sectors.filter { selectedNodes.selection.contains(it.id.toString()) }

  if (config.display.wireframe && !config.display.solid) {
    drawWireframeWorld(sceneRenderer, deck, realm, config, nodes, Vector4(1f))
  } else if (config.display.solid) {
//    globalState.depthEnabled = false
//    globalState.cullFaces = false
//    globalState.blendEnabled = true
//    globalState.blendFunction = Pair(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
//
    val lineHalfColor = Vector4(1f, 1f, 1f, 1f)
////      val lineHalfColor = Vector4(1f, 1f, 1f, 0.5f)
////      drawWireframeWorld(sceneRenderer, worldMesh, realm, lineHalfColor)
//    globalState.depthEnabled = true
//    globalState.cullFaces = true

    val elements = deck.depictions
        .filterValues { it.type == DepictionType.staticMesh }
        .mapNotNull { convertSimpleDepiction(deck, it.key, it.value) }
//    renderElements(sceneRenderer, elements)
    for (element in elements) {
      renderMeshElement(sceneRenderer.renderer, element.copy(material = element.material?.copy(shading = false)))
    }
//    renderElements(sceneRenderer, )
//    for (sector in sectors) {
//      var index = 0
//      for (textureId in sector.textureIndex) {
//        val texture = sceneRenderer.sceneRenderer.mappedTextures[textureId]
//            ?: sceneRenderer.sceneRenderer.textures[textureId.toString()]
//
//        if (texture != null)
//          sceneRenderer.effects.texturedFlat.activate(ObjectShaderConfig(texture = texture))
//        else
//          unbindTexture()
//
//        sector.mesh.drawElement(DrawMethod.triangleFan, index++)
//      }
//    }
//    globalState.depthEnabled = false
//    globalState.cullFaces = false

    if (config.display.wireframe)
      drawWireframeWorld(sceneRenderer, deck, realm, config, nodes, lineHalfColor)
  }

  if (config.display.abstract) {
    drawAbstractNodes(sceneRenderer, nodes)
  }

  globalState.depthEnabled = false

//  if (config.display.normals)
//    renderFaceNormals(sceneRenderer, 0.5f, faces)

  if (config.display.nodeIds) {
    renderNodeIds(sceneRenderer, nodes)
  }

//  sceneRenderer.drawPoint(Vector3(), Vector4(1f, 0f, 0f, 1f), 3f)
  sceneRenderer.drawLine(Vector3(), Vector3(1f, 0f, 0f), red)
  sceneRenderer.drawLine(Vector3(), Vector3(0f, 1f, 0f), green)
  sceneRenderer.drawLine(Vector3(), Vector3(0f, 0f, 1f), blue)
//  sceneRenderer.drawPoint(Vector3(4.253298f, 64.679794f, 0.0f), Vector4(1f, 1f, 0f, 1f), 3f)
//  sceneRenderer.drawPoint(Vector3(3.3128958f, 39.977787f, 0.0f), Vector4(1f, 0f, 1f, 1f), 3f)
//  sceneRenderer.drawPoint(Vector3(16.027586f, 69.140396f, 0.0f), Vector4(0f, 1f, 1f, 1f), 3f)

//  for (id in config.selection) {
//    val face = realm.mesh.faces[id]
//    if (face != null) {
//      face.vertices.forEachIndexed { index, v ->
//        sceneRenderer.drawText(index.toString(), v, LabTextStyles.lessRed)
//      }
//    }
//  }

//  globalState.depthEnabled = true
//  sceneRenderer.drawLine(config.tempStart, config.tempEnd, yellow)
}

fun createOrbitalCamera(camera: MapViewOrbitalCamera): Camera {
  val position = Vector3().transform(Matrix.identity
      .translate(camera.target)
      .rotateZ(camera.yaw)
      .rotateX(-camera.pitch)
      .translate(Vector3(0f, -camera.distance * 4f, 0f))
  )
  return Camera(
      ProjectionType.orthographic,
      position,
      Quaternion(),
      nearClip = 0.001f,
      angleOrZoom = camera.distance,

      lookAt = camera.target
  )
}

fun createFirstPersonCamera(camera: MapViewFirstPersonCamera): Camera {
  return Camera(
      ProjectionType.perspective,
      camera.position,
      Quaternion()
          .rotateZ(camera.yaw)
          .rotateY(camera.pitch),
      45f
  )
}

fun createMapViewCamera(config: MapViewConfig) =
    if (config.cameraMode == MapViewCameraMode.orbital)
      createOrbitalCamera(config.orbitalCamera)
    else
      createFirstPersonCamera(config.firstPersonCamera)

fun renderMapView(client: Client, world: World, deck: Deck, config: MapViewConfig): StateDepiction = { seed ->
  embedCameraView { b, c ->
    val camera = createMapViewCamera(config)
    val windowInfo = client.getWindowInfo()
    val renderer = client.renderer
    prepareRender(renderer, windowInfo)

    val scene = Scene(
        camera = camera,
        lights = listOf(),
        lightingConfig = defaultLightingConfig()
    )
    val sceneRenderer = createSceneRenderer(renderer, scene, b.toVector4i())
    renderMapMesh(sceneRenderer, world.realm, deck, config, seed.bag)
    val navMesh = world.navMesh
    if (navMesh != null)
      renderNavMesh(sceneRenderer.renderer, config.display, navMesh)

    conditionalDrawAiTargets(deck, renderer)

    finishRender(renderer, windowInfo)
  }
}
