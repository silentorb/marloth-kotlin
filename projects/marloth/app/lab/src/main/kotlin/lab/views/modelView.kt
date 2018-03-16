package lab.views

import haft.getCommand
import haft.isActive
import lab.LabCommandType
import mythic.bloom.*
import mythic.drawing.Canvas
import mythic.glowing.DrawMethod
import mythic.glowing.globalState
import mythic.glowing.viewportStack
import mythic.sculpting.FlexibleMesh
import mythic.sculpting.MeshBundle
import mythic.spatial.*
import mythic.typography.TextConfiguration
import org.joml.*
import rendering.*
import rendering.meshes.createHuman
import scenery.Camera
import scenery.ProjectionType
import scenery.Scene
import java.text.DecimalFormat
import lab.utility.*

data class ViewCameraConfig(
    var rotationY: Float = 0f,
    var rotationZ: Float = 0f,
    var pivot: Vector3 = Vector3(),
    var zoom: Float = 2f
)

data class ModelViewConfig(
    var model: MeshType = MeshType.character,
    var drawVertices: Boolean = true,
    var drawEdges: Boolean = true,
    var camera: ViewCameraConfig = ViewCameraConfig(),
    var vertexSelection: MutableList<Int> = mutableListOf(),
    var tempStart: Vector3 = Vector3(),
    var tempEnd: Vector3 = Vector3()
)

typealias MeshGenerator = (FlexibleMesh) -> Unit

private var result: MeshGenerator? = null

fun createOrthographicCamera(camera: ViewCameraConfig): Camera {
  val orientation = Quaternion()
      .rotateZ(camera.rotationZ + Pi)
      .rotateY(camera.rotationY)

  val position = orientation * Vector3(-2f, 0f, 0f) + camera.pivot
//  val position = Vector3(-2f, 0f, 0f)

  return Camera(ProjectionType.orthographic, position, orientation, camera.zoom)
}

fun drawModelPreview(config: ModelViewConfig, renderer: Renderer, b: Bounds, camera: Camera, bundle: MeshBundle) {
  val panelDimensions = Vector2i(b.dimensions.x.toInt(), b.dimensions.y.toInt())
  viewportStack(Vector4i(b.position.x.toInt(), b.position.y.toInt(), panelDimensions.x, panelDimensions.y), {
    val sceneRenderer = renderer.createSceneRenderer(Scene(camera), panelDimensions)
    val transform = Matrix()

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

    sceneRenderer.drawnLine(Vector3(), Vector3(2f, 0f, 0f), red)
    sceneRenderer.drawnLine(Vector3(), Vector3(0f, 2f, 0f), green)
    sceneRenderer.drawnLine(config.tempStart, config.tempEnd, Vector4(1f, 1f, 0f, 1f))

    sceneRenderer.drawnLine(Vector3(), Vector3(0f, 0f, 2f), blue)

    if (config.vertexSelection.size > 0) {
      val vertices = bundle.mesh.distinctVertices
      for (index in config.vertexSelection) {
        sceneRenderer.drawPoint(vertices[index], white, 2f)
      }
    }
    globalState.depthEnabled = false

    for (group in bundle.info.edgeGroups) {
      for (pair in group) {
        sceneRenderer.drawnLine(pair.key.first, pair.key.second, yellow)
      }
    }
  })
}

private fun draw(backgroundColor: Vector4): Render = { b: Bounds, canvas: Canvas ->
  globalState.depthEnabled = false
  drawFill(b, canvas, backgroundColor)
  drawBorder(b, canvas, Vector4(0f, 0f, 0f, 1f))
}

private fun drawScenePanel(config: ModelViewConfig, renderer: Renderer, bundle: MeshBundle, camera: Camera): Render = { b: Bounds, canvas: Canvas ->
  draw(sceneBackgroundColor)(b, canvas)
  drawModelPreview(config, renderer, b, camera, bundle)
}

val decimalFormat = DecimalFormat("#.#####")

fun toString(vector: Vector3) =
    decimalFormat.format(vector.x) + ", " + decimalFormat.format(vector.y) + ", " + decimalFormat.format(vector.z)

private fun drawInfoPanel(config: ModelViewConfig, renderer: Renderer, bundle: MeshBundle,
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

  if (config.vertexSelection.size > 0) {
    val vertices = bundle.mesh.distinctVertices
    drawText(toString(vertices[config.vertexSelection.first()]))
//    canvas.drawText(TextConfiguration(toString(vertices[config.vertexSelection.first()]),
//        renderer.fonts[0], 12f, bounds.position + Vector2(5f, 25f), black))
  }

  drawText("rotationY: " + config.camera.rotationY)
  drawText("rotationZ: " + config.camera.rotationZ)

//  canvas.drawText(TextConfiguration("ts: " + config.tempStart.x.toString() + ", " + config.tempStart.y.toString() + ", " + config.tempStart.z.toString(),
//      renderer.fonts[0], 12f, bounds.position + Vector2(5f, 25f), black))
//  canvas.drawText(TextConfiguration("te: " + config.tempEnd.x.toString() + ", " + config.tempEnd.y.toString() + ", " + config.tempEnd.z.toString(),
//      renderer.fonts[0], 12f, bounds.position + Vector2(5f, 45f), black))
}

private val drawSidePanel = { draw(panelColor) }

private val rotateSpeedZ = 0.04f
private val rotateSpeedY = 0.02f

private fun trySelect(config: ModelViewConfig, camera: Camera, mesh: FlexibleMesh, mousePosition: Vector2i, layout: LabLayout) {
  val bounds = layout.boxes[1].bounds
  val dimensions = bounds.dimensions
  val cursor = mousePosition - bounds.position.toVector2i()
  val cameraData = createCameraEffectsData(dimensions.toVector2i(), camera)
  val viewportBounds = listOf(
      0, 0,
      bounds.dimensions.x.toInt(), bounds.dimensions.y.toInt()
  ).toIntArray()
  val start = cameraData.transform.unproject(cursor.x.toFloat(), bounds.dimensions.y - cursor.y.toFloat(), 0.01f, viewportBounds, Vector3())
//  start.x *= (400 / dimensions.x) * 0.5f
  val end = start + cameraData.direction * camera.farClip
  config.tempStart = start
  config.tempEnd = end
//  config.tempEnd = cameraData.transform.unproject(cursor.x.toFloat(), bounds.dimensions.y - cursor.y.toFloat(), 1f, viewportBounds, Vector3()) + 0.08f
  val vertices = mesh.distinctVertices
  val hits = vertices.filter { rayIntersectsSphere(start, end, it, 0.02f) }
  if (hits.size > 0) {
    val selected = hits.sortedBy { it.distance(start) }.map { vertices.indexOf(it) }
    config.vertexSelection = selected.take(1).toMutableList()
  } else {
    config.vertexSelection = mutableListOf()
  }
}

fun tightenRotation(value: Float): Float =
    value % (Pi * 2)

class ModelView(val config: ModelViewConfig, val renderer: Renderer, val mousePosition: Vector2i) : View {
  val meshBundle: MeshBundle = createHuman()
  val camera = createOrthographicCamera(config.camera)

  override fun createLayout(dimensions: Vector2i): LabLayout {
    val panels = listOf(
        Pair(Measurement(Measurements.pixel, 200f), drawSidePanel()),
        Pair(Measurement(Measurements.stretch, 0f), drawScenePanel(config, renderer, meshBundle, camera)),
        Pair(Measurement(Measurements.pixel, 300f), drawInfoPanel(config, renderer, meshBundle, mousePosition))
    )
    val dimensions2 = Vector2(dimensions.x.toFloat(), dimensions.y.toFloat())
    val boxes = overlap(createVerticalBounds(panels.map { it.first }, dimensions2), panels, { a, b ->
      Box(a, b.second)
    })

    return LabLayout(
        boxes
    )
  }

  override fun updateState(layout: LabLayout, input: InputState, delta: Float) {
    val commands = input.commands

    val rotateSpeedZ = 1f
    val rotateSpeedY = 1f

    if (isActive(commands, LabCommandType.select)) {
      trySelect(config, camera, meshBundle.mesh, mousePosition, layout)
    }

    if (isActive(commands, LabCommandType.rotate)) {
      config.camera.rotationZ += rotateSpeedZ * delta * input.mouseOffset.x
      config.camera.rotationY += rotateSpeedY * delta * input.mouseOffset.y
    }

    if (isActive(commands, LabCommandType.pan) && (input.mouseOffset.x != 0 || input.mouseOffset.y != 0)) {
      val offset = Vector3(0f, input.mouseOffset.x.toFloat(), input.mouseOffset.y.toFloat())
      config.camera.pivot += camera.orientation * offset * delta * config.camera.zoom * 0.14f
    }

    if (isActive(commands, LabCommandType.zoomIn)) {
      config.camera.zoom -= 10 * delta * getCommand(commands, LabCommandType.zoomIn).value
    } else if (isActive(commands, LabCommandType.zoomOut)) {
      config.camera.zoom += 10 * delta * getCommand(commands, LabCommandType.zoomOut).value
    }

    config.camera.rotationY = tightenRotation(config.camera.rotationY)
    config.camera.rotationZ = tightenRotation(config.camera.rotationZ)
  }

  override fun getCommands(): LabCommandMap = mapOf(
      LabCommandType.rotateLeft to { c -> config.camera.rotationZ += rotateSpeedZ * c.value },
      LabCommandType.rotateRight to { c -> config.camera.rotationZ -= rotateSpeedZ * c.value },
      LabCommandType.rotateUp to { c -> config.camera.rotationY += rotateSpeedY * c.value },
      LabCommandType.rotateDown to { c -> config.camera.rotationY -= rotateSpeedY * c.value },
//      LabCommandType.update to { _ -> updateResult2() },
      LabCommandType.cameraViewFront to { _ ->
        config.camera.rotationY = 0f
        config.camera.rotationZ = 0f
      },
      LabCommandType.cameraViewTop to { _ ->
        config.camera.rotationY = Pi / 2
        config.camera.rotationZ = 0f
      }
//      ,
//      LabCommandType.select to { _ ->
//        trySelect(camera, mesh, mousePosition)
//      }
  )
}