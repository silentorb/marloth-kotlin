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
import mythic.sculpting.FlexibleMesh
import mythic.spatial.*
import mythic.typography.TextConfiguration
import org.joml.Vector2i
import org.joml.minus
import org.joml.plus
import org.joml.times
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
    var vertexSelection: MutableList<Int> = mutableListOf()
)

private val black = grayTone(0f)
private val sceneBackgroundColor = grayTone(0.22f)
private val panelColor = grayTone(0.45f)
private val faceColor = grayTone(0.1f, 0.3f)
private val lineColor = black
private val green = Vector4(0f, 1f, 0f, 1f)
private val red = Vector4(1f, 0f, 0f, 1f)
private val blue = Vector4(0f, 0f, 1f, 1f)

typealias MeshGenerator = (FlexibleMesh) -> Unit

private var result: MeshGenerator? = null

fun createOrthographicCamera(camera: ViewCameraConfig): Camera {
  val orientation = Quaternion()
      .rotateY(camera.rotationY)
      .rotateZ(camera.rotationZ + Pi * 0.5f)

  return Camera(ProjectionType.orthographic, orientation * Vector3(-2f, 0f, 1f), orientation, 30f)
}

fun drawModelPreview(renderer: Renderer, dimensions: Vector2i, camera: Camera, sourceMesh: FlexibleMesh) {
  val sceneRenderer = renderer.createSceneRenderer(Scene(camera), dimensions)
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
  sceneRenderer.drawnLine(Vector3(), Vector3(0f, 0f, 2f), blue)
  globalState.depthEnabled = false
}

private fun draw(backgroundColor: Vector4): Render = { b: Bounds, canvas: Canvas ->
  globalState.depthEnabled = false
  drawFill(b, canvas, backgroundColor)
  drawBorder(b, canvas, Vector4(0f, 0f, 0f, 1f))
}

private fun drawScenePanel(config: ModelViewConfig, renderer: Renderer, mesh: FlexibleMesh, camera: Camera): Render = { b: Bounds, canvas: Canvas ->
  draw(sceneBackgroundColor)(b, canvas)
  val panelDimensions = Vector2i(b.dimensions.x.toInt(), b.dimensions.y.toInt())
  drawModelPreview(renderer, panelDimensions, camera, mesh)
}

private fun drawInfoPanel(config: ModelViewConfig, renderer: Renderer, mousePosition: Vector2i): Render = { bounds: Bounds, canvas: Canvas ->
  drawSidePanel()(bounds, canvas)
  canvas.drawText(TextConfiguration("Mouse: " + mousePosition.x.toString() + ", " + mousePosition.y.toString(),
      renderer.fonts[0], 12f, bounds.position + Vector2(5f, 5f), black))
}

private val drawSidePanel = { draw(panelColor) }

private val rotateSpeedZ = 0.04f
private val rotateSpeedY = 0.02f

private fun trySelect(config: ModelViewConfig, camera: Camera, mesh: FlexibleMesh, mousePosition: Vector2i, layout: LabLayout) {
  val dimensions = layout.boxes[1].bounds.dimensions.toVector2i()
//  val cursor = mousePosition - layout.boxes[1].bounds.position.toVector2i()
//  val cameraMatrix = createCameraMatrix(dimensions, camera)
  val view = (Vector3() - camera.position).normalize()
  val h = view.cross(Vector3(0f, 0f, 1f)).normalize()
  val v = h.cross(view).normalize()

}

class ModelView(val config: ModelViewConfig, val renderer: Renderer, val mousePosition: Vector2i) : View {
  val mesh: FlexibleMesh = createHuman()
  val camera = createOrthographicCamera(config.camera)

  override fun createLayout(dimensions: Vector2i): LabLayout {
    val panels = listOf(
        Pair(Measurement(Measurements.pixel, 200f), drawSidePanel()),
        Pair(Measurement(Measurements.stretch, 0f), drawScenePanel(config, renderer, mesh, camera)),
        Pair(Measurement(Measurements.pixel, 200f), drawInfoPanel(config, renderer, mousePosition))
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