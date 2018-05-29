package lab.views.model

import lab.utility.*
import lab.views.renderFaceNormals
import mythic.bloom.*
import mythic.drawing.Canvas
import mythic.glowing.DrawMethod
import mythic.glowing.globalState
import mythic.glowing.viewportStack
import mythic.spatial.*
import org.joml.*
import rendering.*
import rendering.meshes.ModelElement
import rendering.meshes.ModelElements
import rendering.meshes.modelToMeshes
import scenery.Camera
import scenery.ProjectionType
import scenery.Scene

fun createOrthographicCamera(camera: ViewCameraConfig): Camera {
  val orientation = Quaternion()
      .rotateZ(camera.rotationZ)
      .rotateY(camera.rotationY)

  val position = orientation * Vector3(12f, 0f, 0f)
  val orientationSecond = Quaternion().rotateTo(Vector3(1f, 0f, 0f), -position)
  return Camera(ProjectionType.orthographic, position + camera.pivot, orientationSecond, camera.zoom)
}

fun drawMeshPreview(config: ModelViewConfig, sceneRenderer: SceneRenderer, transform: Matrix, section: ModelElement) {
  val mesh = section.mesh

  globalState.depthEnabled = true
  globalState.blendEnabled = true
  globalState.cullFaces = true

  when (config.meshDisplay) {
    MeshDisplay.solid -> sceneRenderer.effects.flat.activate(transform, section.material.color)
    MeshDisplay.wireframe -> sceneRenderer.effects.flat.activate(transform, faceColor)
  }

  mesh.draw(DrawMethod.triangleFan)
  if (config.meshDisplay == MeshDisplay.wireframe) {
    globalState.cullFaces = false
    globalState.depthEnabled = false
  }

  globalState.lineThickness = 1f
  sceneRenderer.effects.flat.activate(transform, lineColor)
  mesh.draw(DrawMethod.lineLoop)

  globalState.pointSize = 3f
  sceneRenderer.effects.flat.activate(transform, lineColor)
  mesh.draw(DrawMethod.points)

  globalState.depthEnabled = false
  globalState.cullFaces = false
}

fun drawSelection(config: ModelViewConfig, model: Model, sceneRenderer: SceneRenderer) {
  if (config.selection.size > 0) {
    when (config.componentMode) {

      ComponentMode.faces -> {
        val faces = model.mesh.faces
        for (index in config.selection) {
          if (faces.size > index) {
            val face = faces[index]
            sceneRenderer.drawSolidFace(face.vertices, white)
          }
        }
      }

      ComponentMode.edges -> {
        val edges = model.edges
        for (index in config.selection) {
          if (edges.size > index) {
            val edge = edges[index]
            sceneRenderer.drawLine(edge.first, edge.second, white, 2f)
          }
        }
      }

      ComponentMode.vertices -> {
        val vertices = model.vertices
        for (index in config.selection) {
          if (vertices.size > index)
            sceneRenderer.drawPoint(vertices[index], white, 2f)
        }
      }

    }
  }
}

fun drawModelPreview(config: ModelViewConfig, renderer: Renderer, b: Bounds, camera: Camera, model: Model,
                     modelElements: ModelElements?) {
  val panelDimensions = Vector2i(b.dimensions.x.toInt(), b.dimensions.y.toInt())
  val viewport = Vector4i(b.position.x.toInt(), b.position.y.toInt(), panelDimensions.x, panelDimensions.y)
  viewportStack(viewport, {
    val sceneRenderer = renderer.createSceneRenderer(Scene(camera), viewport)
    val transform = Matrix()

    if (modelElements != null) {
      modelElements
          .filterIndexed { i, it -> config.visibleGroups[i] }
          .forEach { drawMeshPreview(config, sceneRenderer, transform, it) }
    } else {
      val meshes = modelToMeshes(renderer.vertexSchemas, model)
      meshes
          .filterIndexed { i, it -> config.visibleGroups[i] }
          .forEach {
            drawMeshPreview(config, sceneRenderer, transform, it)
          }
      if (config.drawNormals)
        renderFaceNormals(sceneRenderer, 0.05f, model.mesh)

      meshes.forEach { it.mesh.dispose() }

      sceneRenderer.drawLine(Vector3(), Vector3(1f, 0f, 0f), red)
      sceneRenderer.drawLine(Vector3(), Vector3(0f, 1f, 0f), green)
      sceneRenderer.drawLine(Vector3(), Vector3(0f, 0f, 1f), blue)

      model.mesh.edges.filter { it.faces.none() }.forEach {
        sceneRenderer.drawLine(it.first, it.second, Vector4(0.8f, 0.5f, 0.3f, 1f))
      }

      if (config.drawTempLine)
        sceneRenderer.drawLine(config.tempStart, config.tempEnd, yellow)

      drawSelection(config, model, sceneRenderer)

      globalState.depthEnabled = false

      for (group in model.info.edgeGroups) {
        for (pair in group) {
          sceneRenderer.drawLine(pair.key.first, pair.key.second, yellow)
        }
      }
    }
  })
}

fun drawBackground(backgroundColor: Vector4): Depiction = { b: Bounds, canvas: Canvas ->
  globalState.depthEnabled = false
  drawFill(b, canvas, backgroundColor)
  drawBorder(b, canvas, Vector4(0f, 0f, 0f, 1f))
}


