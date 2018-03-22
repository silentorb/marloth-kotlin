package lab.views.model

import haft.getCommand
import haft.isActive
import lab.LabCommandType
import mythic.bloom.*
import mythic.sculpting.FlexibleMesh
import mythic.sculpting.MeshBundle
import mythic.spatial.*
import org.joml.*
import rendering.*
import scenery.Camera
import lab.views.*
import mythic.sculpting.FlexibleEdge
import mythic.sculpting.getEdgeLoop
import rendering.meshes.createMonster

data class ViewCameraConfig(
    var rotationY: Float = 0f,
    var rotationZ: Float = 0f,
    var pivot: Vector3 = Vector3(),
    var zoom: Float = 2f
)

enum class ComponentMode {
  edges,
  faces,
  vertices
}

data class ModelViewConfig(
    var model: MeshType = MeshType.character,
    var drawVertices: Boolean = true,
    var drawEdges: Boolean = true,
    var camera: ViewCameraConfig = ViewCameraConfig(),
    var selection: List<Int> = listOf(),
    var tempStart: Vector3 = Vector3(),
    var tempEnd: Vector3 = Vector3(),
    var componentMode: ComponentMode = ComponentMode.vertices
)

typealias MeshGenerator = (FlexibleMesh) -> Unit

private var result: MeshGenerator? = null

private val rotateSpeedZ = 0.04f
private val rotateSpeedY = 0.02f

data class Hit(
    val position: Vector3,
    val index: Int
)

fun getVertexHits(start: Vector3, end: Vector3, mesh: FlexibleMesh): List<Hit> {
  val vertices = mesh.distinctVertices
  return vertices.filter { rayIntersectsSphere(start, end, it, 0.02f) }
      .map { Hit(it, vertices.indexOf(it)) }
}

fun getEdgeHits(start: Vector3, end: Vector3, mesh: FlexibleMesh): List<Hit> {
  val edges = mesh.edges
  return edges.mapNotNull {
    val point = rayIntersectsLine(start, end, it.first, it.second, 0.02f)
    if (point != null)
      Hit(point, edges.indexOf(it))
    else
      null
  }
}

fun getHits(componentMode: ComponentMode, start: Vector3, end: Vector3, mesh: FlexibleMesh): List<Hit> =
    when (componentMode) {
      ComponentMode.vertices -> getVertexHits(start, end, mesh)
      ComponentMode.edges -> getEdgeHits(start, end, mesh)
      else -> listOf()
    }

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
  val end = start + cameraData.direction * camera.farClip
  config.tempStart = start
  config.tempEnd = end
  val hits = getHits(config.componentMode, start, end, mesh)
  if (hits.size > 0) {
    val sorted = hits.sortedBy { it.position.distance(start) }
    config.selection = sorted.map { it.index }.take(1).toMutableList()
//    val edge = mesh.edges.filter { it.middle == sorted[0].position }.first()
//    rayIntersectsLine(start, end, edge.first, edge.second, 0.02f)
  } else {
    config.selection = mutableListOf()
  }
}

fun toSelection(mesh: FlexibleMesh, edges: List<FlexibleEdge>) =
    edges.map { mesh.edges.indexOf(it) }

fun selectEdgeLoop(config: ModelViewConfig, mesh: FlexibleMesh) {
  val edge = mesh.edges[config.selection[0]]
  val edges = getEdgeLoop(edge)
  config.selection = toSelection(mesh, edges)
}

fun tightenRotation(value: Float): Float =
    value % (Pi * 2)

fun resetCamera(config: ModelViewConfig, rotationY: Float, rotationZ: Float) {
  config.camera.rotationY = rotationY
  config.camera.rotationZ = rotationZ
  config.camera.pivot = Vector3()
}

class ModelView(val config: ModelViewConfig, val renderer: Renderer, val mousePosition: Vector2i) : View {
  val meshBundle: MeshBundle = createMonster()
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
      config.camera.rotationZ -= rotateSpeedZ * delta * input.mouseOffset.x
      config.camera.rotationY -= rotateSpeedY * delta * input.mouseOffset.y
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

    config.camera.rotationY = Math.min(1.55f, Math.max(-(Pi / 2 - 0.01f), config.camera.rotationY))
    config.camera.rotationZ = tightenRotation(config.camera.rotationZ)
  }

  override fun getCommands(): LabCommandMap = mapOf(
      LabCommandType.rotateLeft to { c -> config.camera.rotationZ -= rotateSpeedZ * c.value },
      LabCommandType.rotateRight to { c -> config.camera.rotationZ += rotateSpeedZ * c.value },
      LabCommandType.rotateUp to { c -> config.camera.rotationY += rotateSpeedY * c.value },
      LabCommandType.rotateDown to { c -> config.camera.rotationY -= rotateSpeedY * c.value },

      LabCommandType.cameraViewFront to { _ ->
        resetCamera(config, 0f, -Pi / 2f)
      },
      LabCommandType.cameraViewBack to { _ ->
        resetCamera(config, 0f, Pi / 2f)
      },
      LabCommandType.cameraViewRight to { _ ->
        resetCamera(config, 0f, Pi)
      },
      LabCommandType.cameraViewLeft to { _ ->
        resetCamera(config, 0f, 0f)
      },
      LabCommandType.cameraViewTop to { _ ->
        resetCamera(config, -1.55f, -Pi / 2)
      },
      LabCommandType.cameraViewBottom to { _ ->
        resetCamera(config, Pi / 2, Pi / 2)
      },

      LabCommandType.toggleSelection to { _ ->
        config.selection = listOf()
      },

      LabCommandType.selectEdgeLoop to { _ ->
        selectEdgeLoop(config, meshBundle.mesh)
      },

      LabCommandType.selectModeEdges to { _ ->
        config.selection = listOf()
        config.componentMode = ComponentMode.edges
      },

      LabCommandType.selectModeVertices to { _ ->
        config.selection = listOf()
        config.componentMode = ComponentMode.vertices
      }
  )
}