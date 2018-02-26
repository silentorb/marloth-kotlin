package lab.views

import groovier.createGroovyShell
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
import mythic.sculpting.FlexibleMesh

private var rotation = Vector3()

val shell = createGroovyShell(
    staticClasses = listOf(
        "rendering.MeshesKt",
        "mythic.sculpting.CreateKt",
        "mythic.sculpting.OperationsKt"
    ),
    aliases = mapOf(
        "org.joml.Vector3f" to "Vector3",
        "org.joml.Vector4f" to "Vector4",
        "org.joml.Matrix4f" to "Matrix"
    ),
    wildcards = listOf(
        "mythic.sculpting",
        "org.joml"
    )
)

fun drawModelPreview(renderer: Renderer, dimensions: Vector2i, orientation: Quaternion, modelName: MeshType) {
  val camera = createCameraEffectsData(dimensions, Camera(Vector3(-6f, 0f, 1f), Quaternion(), 30f))
  val effect = FlatColoredPerspectiveEffect(renderer.shaders.flat, camera)
  val transform = Matrix().rotate(orientation)
//  val mesh = renderer.meshes[modelName]!!

  val script = """
  def mesh = new FlexibleMesh()
  createSphere(mesh, 0.6f, 8, 3)
  translate(mesh.distinctVertices, new Matrix4f().translate(0f, 0f, 1.5f))
  createCylinder(mesh, 0.5f, 8, 1f)
  return mesh
"""

  val sourceMesh = shell.evaluate(script) as FlexibleMesh
  val mesh = createSimpleMesh(sourceMesh, renderer.vertexSchemas.standard, Vector4(0.3f, 0.25f, 0.0f, 1f))

  globalState.depthEnabled = true
  effect.activate(transform, Vector4(0.3f, 0.4f, 0.5f, 0.8f))
  mesh.draw(DrawMethod.triangleFan)

  globalState.lineThickness = 2f
  effect.activate(transform, Vector4(1f, 1f, 0f, 0.6f))
  mesh.draw(DrawMethod.lineLoop)

  globalState.pointSize = 3f
  effect.activate(transform, Vector4(0.1f, 0f, 0f, 0.8f))
  mesh.draw(DrawMethod.points)

  mesh.dispose()
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