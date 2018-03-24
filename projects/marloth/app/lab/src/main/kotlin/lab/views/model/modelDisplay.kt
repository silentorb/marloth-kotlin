package lab.views.model

import lab.utility.*
import mythic.bloom.Bounds
import mythic.bloom.Render
import mythic.drawing.Canvas
import mythic.glowing.DrawMethod
import mythic.glowing.globalState
import mythic.glowing.viewportStack
import mythic.sculpting.MeshBundle
import rendering.Model
import mythic.spatial.*
import mythic.typography.TextConfiguration
import org.joml.*
import rendering.Renderer
import rendering.createSimpleMesh
import scenery.Camera
import scenery.ProjectionType
import scenery.Scene
import java.text.DecimalFormat

fun createOrthographicCamera(camera: ViewCameraConfig): Camera {
  val orientation = Quaternion()
      .rotateZ(camera.rotationZ)
      .rotateY(camera.rotationY)

//      .rotateZ(camera.rotationZ) * Quaternion().rotateY(camera.rotationY)


//      .rotateY(camera.rotationZ)
//      .rotateZ(camera.rotationY)

  val position = orientation * Vector3(12f, 0f, 0f)
  val orientationSecond = Quaternion().rotateTo(Vector3(1f, 0f, 0f), -position)
//  val position = Vector3(-2f, 0f, 0f)

  val k = orientationSecond * Vector3(1f, 0f, 0f)
  return Camera(ProjectionType.orthographic, position + camera.pivot, orientationSecond, camera.zoom)
}

fun drawModelPreview(config: ModelViewConfig, renderer: Renderer, b: Bounds, camera: Camera, model: Model) {
  val panelDimensions = Vector2i(b.dimensions.x.toInt(), b.dimensions.y.toInt())
  viewportStack(Vector4i(b.position.x.toInt(), b.position.y.toInt(), panelDimensions.x, panelDimensions.y), {
    val sceneRenderer = renderer.createSceneRenderer(Scene(camera), panelDimensions)
    val transform = Matrix()

    for (bundle in model.meshes) {
      val simpleMesh = createSimpleMesh(bundle.mesh, renderer.vertexSchemas.standard, Vector4(0.3f, 0.25f, 0.0f, 1f))

      globalState.depthEnabled = true
      globalState.blendEnabled = true
      globalState.cullFaces = true
      sceneRenderer.effects.flat.activate(transform, faceColor)
      simpleMesh.draw(DrawMethod.triangleFan)
      globalState.cullFaces = false

      globalState.depthEnabled = false
      globalState.lineThickness = 1f
      sceneRenderer.effects.flat.activate(transform, lineColor)
      simpleMesh.draw(DrawMethod.lineLoop)

      globalState.pointSize = 3f
      sceneRenderer.effects.flat.activate(transform, lineColor)
      simpleMesh.draw(DrawMethod.points)

      simpleMesh.dispose()
//  renderFaceNormals(renderer,mesh,)
    }

    sceneRenderer.drawLine(Vector3(), Vector3(1f, 0f, 0f), red)
    sceneRenderer.drawLine(Vector3(), Vector3(0f, 1f, 0f), green)
    sceneRenderer.drawLine(Vector3(), Vector3(0f, 0f, 1f), blue)
    sceneRenderer.drawLine(config.tempStart, config.tempEnd, Vector4(1f, 1f, 0f, 1f))

    if (config.selection.size > 0) {
      when (config.componentMode) {
        ComponentMode.vertices -> {
          val vertices = model.meshes.flatMap { it.mesh.distinctVertices }
          for (index in config.selection) {
            if (vertices.size > index)
              sceneRenderer.drawPoint(vertices[index], white, 2f)
          }
        }
        ComponentMode.edges -> {
          val edges = model.meshes.flatMap { it.mesh.edges }
          for (index in config.selection) {
            if (edges.size > index) {
              val edge = edges[index]
              sceneRenderer.drawLine(edge.first, edge.second, white, 2f)
            }
          }
        }
      }
    }
    globalState.depthEnabled = false

    for (bundle in model.meshes) {
      for (group in bundle.info.edgeGroups) {
        for (pair in group) {
          sceneRenderer.drawLine(pair.key.first, pair.key.second, yellow)
        }
      }
    }
  })
}

private fun draw(backgroundColor: Vector4): Render = { b: Bounds, canvas: Canvas ->
  globalState.depthEnabled = false
  drawFill(b, canvas, backgroundColor)
  drawBorder(b, canvas, Vector4(0f, 0f, 0f, 1f))
}

fun drawScenePanel(config: ModelViewConfig, renderer: Renderer, model: Model, camera: Camera): Render = { b: Bounds, canvas: Canvas ->
  draw(sceneBackgroundColor)(b, canvas)
  drawModelPreview(config, renderer, b, camera, model)
}

val decimalFormat = DecimalFormat("#.#####")

fun toString(vector: Vector3) =
    decimalFormat.format(vector.x) + ", " + decimalFormat.format(vector.y) + ", " + decimalFormat.format(vector.z)

fun drawInfoPanel(config: ModelViewConfig, renderer: Renderer, model: Model,
                  mousePosition: Vector2i): Render = { bounds: Bounds, canvas: Canvas ->
  drawSidePanel()(bounds, canvas)
  var row = 1
  fun drawText(content: String) {
    canvas.drawText(TextConfiguration(content,
        renderer.fonts[0], 12f, bounds.position + Vector2(5f, 5f + row++ * 20f), black))
  }
  drawText("Mouse: " + mousePosition.x.toString() + ", " + mousePosition.y.toString())
//  canvas.drawText(TextConfiguration("Mouse: " + mousePosition.x.toString() + ", " + mousePosition.y.toString(),
//      renderer.fonts[0], 12f, bounds.position + Vector2(5f, 5f), black))

  if (config.selection.size > 0) {
    when (config.componentMode) {
      ComponentMode.vertices -> {
        val vertices = model.meshes.flatMap { it.mesh.distinctVertices }
        if (vertices.size > config.selection.first())
          drawText(toString(vertices[config.selection.first()]))
      }
    }
//    canvas.drawText(TextConfiguration(toString(vertices[config.vertexSelection.first()]),
//        renderer.fonts[0], 12f, bounds.position + Vector2(5f, 25f), black))
  }

  drawText("rotationY: " + config.camera.rotationY)
  drawText("rotationZ: " + config.camera.rotationZ)
  drawText("pivot: " + toString(config.camera.pivot))
//  canvas.drawText(TextConfiguration("ts: " + config.tempStart.x.toString() + ", " + config.tempStart.y.toString() + ", " + config.tempStart.z.toString(),
//      renderer.fonts[0], 12f, bounds.position + Vector2(5f, 25f), black))
//  canvas.drawText(TextConfiguration("te: " + config.tempEnd.x.toString() + ", " + config.tempEnd.y.toString() + ", " + config.tempEnd.z.toString(),
//      renderer.fonts[0], 12f, bounds.position + Vector2(5f, 45f), black))
}

val drawSidePanel = { draw(panelColor) }
