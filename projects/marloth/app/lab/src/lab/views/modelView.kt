package lab.views

import lab.LabCommandType
import lab.ModelViewConfig
import mythic.bloom.*
import mythic.drawing.Canvas
import mythic.glowing.DrawMethod
import mythic.glowing.globalState
import mythic.spatial.*
import org.joml.Vector2i
import rendering.*
import scenery.Camera

private var rotation = Vector3()

fun drawModelPreview(renderer: Renderer, dimensions: Vector2i, orientation: Quaternion, modelName: String) {
  val camera = createCameraEffectsData(dimensions, Camera(Vector3(-5f, 0f, 0f), Quaternion(), 30f))
  val effect = FlatColoredPerspectiveEffect(renderer.shaders.flat, camera)
  val transform = Matrix().rotate(orientation)
  val mesh = renderer.meshes[modelName]!!

  globalState.depthEnabled = true
  effect.activate(transform, Vector4(0.3f, 0.4f, 0.5f, 0.8f))
  mesh.draw(DrawMethod.triangleFan)

  globalState.lineThickness = 2f
  effect.activate(transform, Vector4(1f, 1f, 0f, 0.6f))
  mesh.draw(DrawMethod.lineLoop)

  globalState.pointSize = 3f
  effect.activate(transform, Vector4(0.1f, 0f, 0f, 0.8f))
  mesh.draw(DrawMethod.points)

//  renderFaceNormals(renderer,mesh,)

  globalState.depthEnabled = false
}

class ModelView(val config: ModelViewConfig, val renderer: Renderer) : View {

  override fun createLayout(dimensions: Vector2i): LabLayout {
    val draw = { b: Bounds, c: Canvas -> drawBorder(b, c, Vector4(0f, 0f, 1f, 1f)) }
    val orientation = Quaternion()
        .rotateZ(rotation.z)
        .rotateY(rotation.y)

    val panels = listOf(
        Pair(Measurement(Measurements.pixel, 200f), draw),
        Pair(Measurement(Measurements.stretch, 0f), { b: Bounds, c: Canvas ->
          draw(b, c)
          val panelDimensions = Vector2i(b.dimensions.x.toInt(), b.dimensions.y.toInt())
          drawModelPreview(renderer, panelDimensions, orientation, config.model)
        })
    )
    val dimensions2 = Vector2(dimensions.x.toFloat(), dimensions.y.toFloat())
    val boxes = overlap(createVerticalBounds(panels.map { it.first }, dimensions2), panels, { a, b ->
      Box(a, b.second)
    })

    return LabLayout(
        boxes
    )
  }

  val rotateSpeedZ = 0.04f
  val rotateSpeedY = 0.02f

  override fun getCommands(): LabCommandMap = mapOf(
      LabCommandType.rotateLeft to { _ -> rotation.z += rotateSpeedZ },
      LabCommandType.rotateRight to { _ -> rotation.z -= rotateSpeedZ },
      LabCommandType.rotateUp to { _ -> rotation.y += rotateSpeedY },
      LabCommandType.rotateDown to { _ -> rotation.y -= rotateSpeedY }
  )
}