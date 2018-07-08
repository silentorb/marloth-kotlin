package lab.views.map

import lab.views.renderFaceNormals
import marloth.clienting.Client
import mythic.glowing.DrawMethod
import mythic.glowing.globalState
import mythic.spatial.*
import mythic.typography.TextStyle
import org.joml.Vector4i
import org.joml.minus
import org.joml.plus
import org.lwjgl.opengl.GL11
import rendering.*
import scenery.Camera
import scenery.ProjectionType
import scenery.Scene
import simulation.AbstractWorld
import simulation.getFaceInfo

fun drawWireframeWorld(renderer: SceneRenderer, worldMesh: WorldMesh, world: AbstractWorld, color: Vector4) {
  for (node in world.nodes) {
    var index = 0
    for (face in node.faces) {
//      renderer.effects.flat.activate(ObjectShaderConfig(color = color))
//      renderer.drawOutlinedFace(face.vertices, color)
      val debugInfo = getFaceInfo(face).debugInfo
      val c = when (debugInfo) {
        "lower" -> Vector4(1f, 0f, 1f, 1f)
        else -> color
      }
      for (edge in face.edges) {
        renderer.drawLine(edge.first, edge.second, c)
//                sector.mesh.drawElement(DrawMethod.lineLoop, index++)
      }
    }
  }
}

fun renderFaceIndices(renderer: SceneRenderer, world: AbstractWorld) {
  globalState.depthEnabled = true
  val textStyle = TextStyle(renderer.renderer.fonts[0], 0f, Vector4(0.5f, 1f, 1f, 1f))
  for (node in world.nodes) {
    for (face in node.faces) {
      val normalOffset = face.normal * 0.5f
      face.edges.forEachIndexed { index, edge ->
        val vertex = edge.first
        val centeringOffset = (edge.next!!.first - vertex).normalize() + (edge.previous!!.first - vertex).normalize()
        val offset = centeringOffset// + normalOffset
        renderer.drawText(index.toString(), vertex + offset, textStyle)
      }
    }
  }
}

fun renderMapMesh(renderer: SceneRenderer, world: AbstractWorld, config: MapViewDisplayConfig) {
  val worldMesh = renderer.renderer.worldMesh
  if (worldMesh != null) {
    if (config.drawMode == MapViewDrawMode.wireframe) {
      drawWireframeWorld(renderer, worldMesh, world, Vector4(1f))
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
        for (texture in sector.textureIndex) {
          renderer.effects.texturedFlat.activate(ObjectShaderConfig(texture = texture))
          sector.mesh.drawElement(DrawMethod.triangleFan, index++)
        }
      }
      globalState.depthEnabled = false
      globalState.cullFaces = false
      drawWireframeWorld(renderer, worldMesh, world, lineHalfColor)
    }

    if (config.normals)
      renderFaceNormals(renderer, 1f, world.mesh)

    if (config.vertexIndices) {
      renderFaceIndices(renderer, world)
    }
  }
}

fun createTopDownCamera(camera: MapViewCamera): Camera {
  val position = Vector3().transform(Matrix()
      .translate(camera.target)
      .rotateZ(camera.yaw)
      .translate(Vector3(0f, -camera.distance, camera.distance))
  )
  return Camera(
      ProjectionType.perspective,
      position,
      Quaternion()
//      Quaternion().rotateZ(Pi * 0.5f - camera.yaw)
//          *
//          Quaternion().rotateY(Pi * 0.25f)
      ,
      45f,
      lookAt = camera.target
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
  renderMapMesh(sceneRenderer, world, config.display)
  renderer.finishRender(windowInfo)
}