package lab.views.model

import haft.getCommand
import haft.isActive
import lab.LabCommandType
import mythic.bloom.*
import mythic.spatial.*
import org.joml.*
import rendering.*
import scenery.Camera
import lab.views.*
import mythic.sculpting.*

data class ModelLayout(
    val boxes: List<Box>,
    val modelPanelBounds: Bounds,
    val clickBoxes: List<ClickBox<SelectionEvent>>
)

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

enum class MeshDisplay {
  solid,
  wireframe
}

data class ModelViewConfig(
    var model: MeshType = MeshType.bear,
    var drawVertices: Boolean = true,
    var drawEdges: Boolean = true,
    var camera: ViewCameraConfig = ViewCameraConfig(),
    var selection: List<Int> = listOf(),
    var tempStart: Vector3 = Vector3(),
    var tempEnd: Vector3 = Vector3(),
    var componentMode: ComponentMode = ComponentMode.vertices,
    var meshDisplay: MeshDisplay = MeshDisplay.solid,
    var drawNormals: Boolean = false
)

typealias MeshGenerator = (FlexibleMesh) -> Unit

private var result: MeshGenerator? = null

private val rotateSpeedZ = 0.04f
private val rotateSpeedY = 0.02f

data class Hit(
    val position: Vector3,
    val index: Int
)

fun getVertexHits(start: Vector3, end: Vector3, model: Model): List<Hit> {
  val vertices = model.vertices
  return vertices.filter { rayIntersectsSphere(start, end, it, 0.02f) }
      .map { Hit(it, vertices.indexOf(it)) }
}

fun getEdgeHits(start: Vector3, end: Vector3, model: Model): List<Hit> {
  val edges = model.edges
  return edges.mapNotNull {
    val point = rayIntersectsLine3D(start, end, it.first, it.second, 0.02f)
    if (point != null)
      Hit(point, edges.indexOf(it))
    else
      null
  }
}

fun getFaceHits(start: Vector3, end: Vector3, model: Model): List<Hit> {
  val faces = model.mesh.faces
  val rayDirection = (end - start).normalize()

  return faces.mapIndexedNotNull { i, it ->
    if (it.normal.x == 0f && it.normal.y == 0f && it.normal.z == 0f)
      it.updateNormal()

    val point = rayIntersectsPolygon3D(start, rayDirection, it.vertices, it.normal)
    if (point != null)
      Hit(point, i)
    else
      null
  }
}

fun getHits(componentMode: ComponentMode, start: Vector3, end: Vector3, model: Model): List<Hit> =
    when (componentMode) {
      ComponentMode.vertices -> getVertexHits(start, end, model)
      ComponentMode.edges -> getEdgeHits(start, end, model)
      ComponentMode.faces -> getFaceHits(start, end, model)
      else -> listOf()
    }

private fun trySelect(config: ModelViewConfig, camera: Camera, model: Model, mousePosition: Vector2i, bounds: Bounds) {
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
  val hits = getHits(config.componentMode, start, end, model)
  if (hits.size > 0) {
    val sorted = hits.sortedBy { it.position.distance(start) }
    config.selection = sorted.take(1).map { it.index }.toMutableList()
//    val edge = mesh.edges.filter { it.middle == sorted[0].position }.first()
//    rayIntersectsLine3D(start, end, edge.first, edge.second, 0.02f)
  } else {
    config.selection = mutableListOf()
  }
}

fun toSelection(model: Model, edges: List<FlexibleEdge>) =
    edges.map { model.edges.indexOf(it) }

fun selectEdgeLoop(config: ModelViewConfig, model: Model) {
  val edge = model.edges[config.selection[0]]
  val edges = getEdgeLoop(edge)
  config.selection = toSelection(model, edges)
}

fun tightenRotation(value: Float): Float =
    value % (Pi * 2)

fun resetCamera(config: ModelViewConfig, model: Model, rotationY: Float, rotationZ: Float) {
  config.camera.rotationY = rotationY
  config.camera.rotationZ = rotationZ
  config.camera.pivot = getVerticesCenter(model.vertices)
}

data class SelectionEvent(
    val index: Int
)

fun drawLeftPanel(meshTypes: List<MeshType>, config: ModelViewConfig) = { bounds: Bounds ->
  val padding = Vector2(10f)
  val itemHeight = 30f
  val focusIndex = meshTypes.indexOf(config.model)

  val items = meshTypes
      .map { it.name }
      .mapIndexed { index, it -> PartialBox(itemHeight, drawListItem(it, focusIndex == index)) }

  val buttonBoxes = arrangeList(verticalArrangement(padding), items, bounds)
  val boxes = listOf(
      Box(bounds, drawSidePanel())
  )
      .plus(buttonBoxes)
  Pair(boxes, buttonBoxes.mapIndexed { i, b -> ClickBox(b.bounds, SelectionEvent(i)) })
}

fun loadGeneratedModel(config: ModelViewConfig, renderer: Renderer): Model {
  val generator = renderer.meshGenerators[config.model]
  return if (generator != null)
    generator()
  else
    Model(FlexibleMesh(), listOf())
}

fun loadExternalMesh(config: ModelViewConfig, renderer: Renderer): ModelElements? {
  val generator = renderer.meshGenerators[config.model]
  return if (generator != null)
    null
  else
    renderer.meshes[config.model]
}

class ModelView(val config: ModelViewConfig, val renderer: Renderer, val mousePosition: Vector2i) {
  val model: Model = loadGeneratedModel(config, renderer)
  val externalMesh: ModelElements? = loadExternalMesh(config, renderer)
  val camera = createOrthographicCamera(config.camera)

  fun createLayout(dimensions: Vector2i): ModelLayout {
    val bounds = Bounds(Vector2(), dimensions.toVector2())
    val initialLengths = listOf(200f, null, 300f)

    val left = drawLeftPanel(renderer.meshes.keys.toList(), config)
    val middle = { b: Bounds -> Box(b, drawScenePanel(config, renderer, model, camera, externalMesh)) }
    val right = { b: Bounds -> Box(b, drawInfoPanel(config, renderer, model, mousePosition)) }
    val lengths = solveMeasurements(dimensions.x.toFloat(), initialLengths)
    val panelBounds = arrangeList2(horizontalArrangement(Vector2(0f, 0f)), lengths, bounds)
    val boxes = panelBounds.drop(1)
        .zip(listOf(middle, right), { b, p -> p(b) })

    val (leftBoxes, leftClickBoxes) = left(panelBounds[0])

    return ModelLayout(
        boxes = leftBoxes
            .plus(boxes),
        modelPanelBounds = panelBounds[1],
        clickBoxes = leftClickBoxes
    )
  }

  fun updateState(layout: ModelLayout, input: InputState, delta: Float) {
    val commands = input.commands

    val rotateSpeedZ = 1f
    val rotateSpeedY = 1f

    if (isActive(commands, LabCommandType.select)) {
      val clickBox = filterMouseOverBoxes(layout.clickBoxes, mousePosition)
      if (clickBox != null) {
        config.model = renderer.meshes.keys.toList()[clickBox.value.index]
      } else {
        trySelect(config, camera, model, mousePosition, layout.modelPanelBounds)
      }
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

  fun getCommands(): LabCommandMap = mapOf(
      LabCommandType.rotateLeft to { c -> config.camera.rotationZ -= rotateSpeedZ * c.value },
      LabCommandType.rotateRight to { c -> config.camera.rotationZ += rotateSpeedZ * c.value },
      LabCommandType.rotateUp to { c -> config.camera.rotationY += rotateSpeedY * c.value },
      LabCommandType.rotateDown to { c -> config.camera.rotationY -= rotateSpeedY * c.value },

      LabCommandType.cameraViewFront to { _ ->
        resetCamera(config, model, 0f, -Pi / 2f)
      },
      LabCommandType.cameraViewBack to { _ ->
        resetCamera(config, model, 0f, Pi / 2f)
      },
      LabCommandType.cameraViewRight to { _ ->
        resetCamera(config, model, 0f, Pi)
      },
      LabCommandType.cameraViewLeft to { _ ->
        resetCamera(config, model, 0f, 0f)
      },
      LabCommandType.cameraViewTop to { _ ->
        resetCamera(config, model, -1.55f, -Pi / 2)
      },
      LabCommandType.cameraViewBottom to { _ ->
        resetCamera(config, model, Pi / 2, Pi / 2)
      },

      LabCommandType.toggleMeshDisplay to { _ ->
        config.meshDisplay = if (config.meshDisplay == MeshDisplay.wireframe)
          MeshDisplay.solid
        else
          MeshDisplay.wireframe
      },

      LabCommandType.toggleSelection to { _ ->
        config.selection = listOf()
      },

      LabCommandType.selectEdgeLoop to { _ ->
        selectEdgeLoop(config, model)
      },

      LabCommandType.toggleNormals to { _ ->
        config.drawNormals = !config.drawNormals
      },

      LabCommandType.selectModeEdges to { _ ->
        config.selection = listOf()
        config.componentMode = ComponentMode.edges
      },

      LabCommandType.selectModeFaces to { _ ->
        config.selection = listOf()
        config.componentMode = ComponentMode.faces
      },

      LabCommandType.selectModeVertices to { _ ->
        config.selection = listOf()
        config.componentMode = ComponentMode.vertices
      }
  )
}