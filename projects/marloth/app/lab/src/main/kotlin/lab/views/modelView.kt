package lab.views

import haft.Command
import haft.isActive
import lab.LabCommandType
import lab.utility.drawBorder
import lab.utility.drawFill
import lab.utility.grayTone
import mythic.bloom.*
import mythic.drawing.Canvas
import mythic.glowing.DrawMethod
import mythic.glowing.globalState
import mythic.glowing.viewportStack
import mythic.sculpting.FlexibleMesh
import mythic.spatial.*
import mythic.typography.TextConfiguration
import org.joml.*
import rendering.*
import rendering.meshes.createHuman
import scenery.Camera
import scenery.ProjectionType
import scenery.Scene

data class ViewCameraConfig(
    var rotationY: Float = 0f,
    var rotationZ: Float = 0f
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

private val black = grayTone(0f)
private val sceneBackgroundColor = grayTone(0.22f)
private val panelColor = grayTone(0.45f)
private val faceColor = grayTone(0.1f, 0.3f)
private val lineColor = black
private val green = Vector4(0f, 1f, 0f, 1f)
private val red = Vector4(1f, 0f, 0f, 1f)
private val blue = Vector4(0f, 0f, 1f, 1f)
private val white = Vector4(1f)

typealias MeshGenerator = (FlexibleMesh) -> Unit

private var result: MeshGenerator? = null

fun createOrthographicCamera(camera: ViewCameraConfig): Camera {
  val orientation = Quaternion()
      .rotateY(camera.rotationY)
      .rotateZ(camera.rotationZ + Pi * 0.5f)

//  return Camera(ProjectionType.perspective, orientation * Vector3(-12f, 0f, 1f), orientation, 30f)
  return Camera(ProjectionType.orthographic, orientation * Vector3(-2f, 0f, 1f), orientation, 30f)
}

fun drawModelPreview(config: ModelViewConfig, renderer: Renderer, b: Bounds, camera: Camera, sourceMesh: FlexibleMesh) {
  val panelDimensions = Vector2i(b.dimensions.x.toInt(), b.dimensions.y.toInt())
  viewportStack(Vector4i(b.position.x.toInt(), b.position.y.toInt(), panelDimensions.x, panelDimensions.y), {
    val sceneRenderer = renderer.createSceneRenderer(Scene(camera), panelDimensions)
//  val cameraData = createCameraEffectsData(dimensions, camera)
//  val effect = FlatColoredPerspectiveEffect(renderer.shaders.flat, cameraData)
    val transform = Matrix()

    val simpleMesh = createSimpleMesh(sourceMesh, renderer.vertexSchemas.standard, Vector4(0.3f, 0.25f, 0.0f, 1f))

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
      val vertices = sourceMesh.distinctVertices
      for (index in config.vertexSelection) {
        sceneRenderer.drawPoint(vertices[index], white, 2f)
      }
    }
    globalState.depthEnabled = false
  })
}

private fun draw(backgroundColor: Vector4): Render = { b: Bounds, canvas: Canvas ->
  globalState.depthEnabled = false
  drawFill(b, canvas, backgroundColor)
  drawBorder(b, canvas, Vector4(0f, 0f, 0f, 1f))
}

private fun drawScenePanel(config: ModelViewConfig, renderer: Renderer, mesh: FlexibleMesh, camera: Camera): Render = { b: Bounds, canvas: Canvas ->
  draw(sceneBackgroundColor)(b, canvas)
  drawModelPreview(config, renderer, b, camera, mesh)
}

fun toString(vector: Vector3) =
    vector.x.toString() + ", " + vector.y.toString() + ", " + vector.z.toString()

private fun drawInfoPanel(config: ModelViewConfig, renderer: Renderer, mesh: FlexibleMesh,
                          mousePosition: Vector2i): Render = { bounds: Bounds, canvas: Canvas ->
  drawSidePanel()(bounds, canvas)
  canvas.drawText(TextConfiguration("Mouse: " + mousePosition.x.toString() + ", " + mousePosition.y.toString(),
      renderer.fonts[0], 12f, bounds.position + Vector2(5f, 5f), black))

  if (config.vertexSelection.size > 0) {
    val vertices = mesh.distinctVertices
    canvas.drawText(TextConfiguration(toString(vertices[config.vertexSelection.first()]),
        renderer.fonts[0], 12f, bounds.position + Vector2(5f, 25f), black))
  }
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
  }
  else {
    config.vertexSelection = mutableListOf()
  }
}

class ModelView(val config: ModelViewConfig, val renderer: Renderer, val mousePosition: Vector2i) : View {
  val mesh: FlexibleMesh = createHuman()
  val camera = createOrthographicCamera(config.camera)

  override fun createLayout(dimensions: Vector2i): LabLayout {
    val panels = listOf(
        Pair(Measurement(Measurements.pixel, 200f), drawSidePanel()),
        Pair(Measurement(Measurements.stretch, 0f), drawScenePanel(config, renderer, mesh, camera)),
        Pair(Measurement(Measurements.pixel, 300f), drawInfoPanel(config, renderer, mesh, mousePosition))
    )
    val dimensions2 = Vector2(dimensions.x.toFloat(), dimensions.y.toFloat())
    val boxes = overlap(createVerticalBounds(panels.map { it.first }, dimensions2), panels, { a, b ->
      Box(a, b.second)
    })

    return LabLayout(
        boxes
    )
  }

  override fun handleInput(layout: LabLayout, commands: List<Command<LabCommandType>>) {
    if (isActive(commands, LabCommandType.select)) {
      trySelect(config, camera, mesh, mousePosition, layout)
    }
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