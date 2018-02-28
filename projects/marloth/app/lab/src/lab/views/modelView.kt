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
import scenery.ProjectionType

private var rotation = Vector3()

//val shell = createGroovyShell(
//    staticClasses = listOf(
//        "rendering.MeshesKt",
//        "mythic.sculpting.CreateKt",
//        "mythic.sculpting.OperationsKt"
//    ),
//    aliases = mapOf(
//        "org.joml.Vector3f" to "Vector3",
//        "org.joml.Vector4f" to "Vector4",
//        "org.joml.Matrix4f" to "Matrix"
//    ),
//    wildcards = listOf(
//        "mythic.sculpting",
//        "org.joml"
//    )
//)
private val backgroundGray = 0.22f
private val faceGray = 0.1f
private val lineColor = Vector4(0f, 0f, 0f, 1f)

fun drawModelPreview(renderer: Renderer, dimensions: Vector2i, orientation: Quaternion, modelName: MeshType) {
  val camera = Camera(ProjectionType.orthographic, Vector3(-2f, 0f, 1f), Quaternion(), 30f)
  val cameraData = createCameraEffectsData(dimensions, camera)
  val effect = FlatColoredPerspectiveEffect(renderer.shaders.flat, cameraData)
  val transform = Matrix().rotate(orientation)
//  val mesh = renderer.meshes[modelName]!!

  val script = """
  def mesh = new FlexibleMesh()
  createSphere(mesh, 0.6f, 8, 3)
  translate(mesh.distinctVertices, new Matrix4f().translate(0f, 0f, 1.5f))
  createCylinder(mesh, 0.5f, 8, 1f)
  return mesh
"""

//  val sourceMesh = shell.evaluate(script) as FlexibleMesh
  val sourceMesh = createHumanoid()
  val mesh = createSimpleMesh(sourceMesh, renderer.vertexSchemas.standard, Vector4(0.3f, 0.25f, 0.0f, 1f))

  globalState.depthEnabled = true
  globalState.blendEnabled = true
  globalState.cullFaces = true
  effect.activate(transform, Vector4(faceGray, faceGray, faceGray, 0.3f))
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

class ModelView(val config: ModelViewConfig, val renderer: Renderer) : View {

  override fun createLayout(dimensions: Vector2i): LabLayout {
    val draw = { b: Bounds, canvas: Canvas ->
      globalState.depthEnabled = false
      canvas.drawSquare(b.position, b.dimensions, canvas.solid(Vector4(backgroundGray, backgroundGray, backgroundGray, 1f)))
      drawBorder(b, canvas, Vector4(0f, 0f, 1f, 1f))
    }
    val orientation = Quaternion()
        .rotateY(rotation.y)
        .rotateZ(rotation.z)

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
      LabCommandType.rotateLeft to { c -> rotation.z += rotateSpeedZ * c.value },
      LabCommandType.rotateRight to { c -> rotation.z -= rotateSpeedZ * c.value }
      ,
      LabCommandType.rotateUp to { c -> rotation.y += rotateSpeedY * c.value },
      LabCommandType.rotateDown to { c -> rotation.y -= rotateSpeedY * c.value }
  )
}