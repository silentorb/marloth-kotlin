package lab.views

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
import org.joml.plus
import rendering.*
import rendering.meshes.createHuman
import scenery.Camera
import scenery.ProjectionType

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

typealias MeshGenerator = (FlexibleMesh) -> Unit

private var result: MeshGenerator? = null

fun drawModelPreview(renderer: Renderer, dimensions: Vector2i, orientation: Quaternion, modelName: MeshType) {
  val camera = Camera(ProjectionType.orthographic, Vector3(-2f, 0f, 1f), Quaternion(), 30f)
  val cameraData = createCameraEffectsData(dimensions, camera)
  val effect = FlatColoredPerspectiveEffect(renderer.shaders.flat, cameraData)
  val transform = Matrix().rotate(orientation)

  val sourceMesh = createHuman()
  val mesh = createSimpleMesh(sourceMesh, renderer.vertexSchemas.standard, Vector4(0.3f, 0.25f, 0.0f, 1f))

  globalState.depthEnabled = true
  globalState.blendEnabled = true
  globalState.cullFaces = true
  effect.activate(transform, faceColor)
  mesh.draw(DrawMethod.triangleFan)
  globalState.cullFaces = false

  globalState.depthEnabled = false
  globalState.lineThickness = 1f
  effect.activate(transform, lineColor)
  mesh.draw(DrawMethod.lineLoop)

  globalState.pointSize = 3f
  effect.activate(transform, lineColor)
  mesh.draw(DrawMethod.points)

  mesh.dispose()
//  renderFaceNormals(renderer,mesh,)

  globalState.depthEnabled = false
}

private fun draw(backgroundColor: Vector4): Render = { b: Bounds, canvas: Canvas ->
  globalState.depthEnabled = false
  drawFill(b, canvas, backgroundColor)
  drawBorder(b, canvas, Vector4(0f, 0f, 0f, 1f))
}

private fun drawScenePanel(config: ModelViewConfig, renderer: Renderer): Render = { b: Bounds, canvas: Canvas ->
  val orientation = Quaternion()
      .rotateY(config.camera.rotationY)
      .rotateZ(config.camera.rotationZ - Pi * 0.5f)

  draw(sceneBackgroundColor)(b, canvas)
  val panelDimensions = Vector2i(b.dimensions.x.toInt(), b.dimensions.y.toInt())
  drawModelPreview(renderer, panelDimensions, orientation, config.model)
}

private fun drawInfoPanel(config: ModelViewConfig, renderer: Renderer, mousePosition: Vector2i): Render = { bounds: Bounds, canvas: Canvas ->
  drawSidePanel()(bounds, canvas)
  canvas.drawText(TextConfiguration("Mouse: " + mousePosition.x.toString() + ", " + mousePosition.y.toString(),
      renderer.fonts[0], 12f, bounds.position + Vector2(5f, 5f), black))
}

private val drawSidePanel = { draw(panelColor) }

private val rotateSpeedZ = 0.04f
private val rotateSpeedY = 0.02f

class ModelView(val config: ModelViewConfig, val renderer: Renderer, val mousePosition: Vector2i) : View {

  override fun createLayout(dimensions: Vector2i): LabLayout {
    val panels = listOf(
        Pair(Measurement(Measurements.pixel, 200f), drawSidePanel()),
        Pair(Measurement(Measurements.stretch, 0f), drawScenePanel(config, renderer)),
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
      },
      LabCommandType.select to { _ ->

      }
  )
}