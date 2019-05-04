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
import scenery.AnimationId

data class ModelLayout(
    val boxes: List<FlatBox>,
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
    var visibleGroups: MutableList<Boolean> = mutableListOf(),
    var tempStart: Vector3 = Vector3(),
    var tempEnd: Vector3 = Vector3(),
    var componentMode: ComponentMode = ComponentMode.vertices,
    var meshDisplay: MeshDisplay = MeshDisplay.solid,
    var drawNormals: Boolean = false,
    var drawTempLine: Boolean = false
)

data class ModelViewState(
    val scrollOffsets: Map<String, Float>,
    val animation: AnimationId,
    val animationElapsedTime: Float
)

fun newModelViewState() =
    ModelViewState(
        scrollOffsets = mapOf(),
        animation = AnimationId.walk,
        animationElapsedTime = 0f
    )

typealias MeshGenerator = (ImmutableMesh) -> Unit

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

private fun getFaceHits(start: Vector3, end: Vector3, model: Model): List<Hit> {
  val faces = model.mesh.faces
  val rayDirection = (end - start).normalize()

  return faces.values.mapIndexedNotNull { i, it ->
    if (it.normal.x == 0f && it.normal.y == 0f && it.normal.z == 0f)
      assert(false)
//      it.updateNormal()

    val point = rayIntersectsPolygon3D(start, rayDirection, it.vertices, it.normal)
    if (point != null)
      Hit(point, i)
    else
      null
  }
}

private fun getHits(componentMode: ComponentMode, start: Vector3, end: Vector3, model: Model): List<Hit> =
    when (componentMode) {
      ComponentMode.vertices -> getVertexHits(start, end, model)
      ComponentMode.edges -> getEdgeHits(start, end, model)
      ComponentMode.faces -> getFaceHits(start, end, model)
      else -> listOf()
    }

private fun trySelect(config: ModelViewConfig, camera: Camera, model: Model, mousePosition: Vector2, bounds: Bounds) {
  val dimensions = bounds.dimensions
  val cursor = mousePosition.toVector2i() - bounds.position
  val cameraData = createCameraEffectsData(dimensions, camera)
  val viewportBounds = listOf(
      0, 0,
      bounds.dimensions.x.toInt(), bounds.dimensions.y.toInt()
  ).toIntArray()
  val start = Vector3(cameraData.transform.unproject(cursor.x.toFloat(),
      bounds.dimensions.y - cursor.y.toFloat(), 0.01f, viewportBounds, Vector3m()))
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

fun toSelection(model: Model, edges: List<ImmutableEdge>) =
    edges.map { model.edges.indexOf(it) }

fun selectEdgeLoop(config: ModelViewConfig, model: Model) {
  val edge = model.edges[config.selection[0]]
  throw Error("Original Edge loop code was a hack and broke.  Code needs to be redesigned.")
//  val edges = getEdgeLoop(edge)
//  config.selection = toSelection(model, edges)
}

fun tightenRotation(value: Float): Float =
    value % (Pi * 2)

fun resetCamera(config: ModelViewConfig, model: AdvancedModel, rotationY: Float, rotationZ: Float) {
  config.camera.rotationY = rotationY
  config.camera.rotationZ = rotationZ
  val m = model.model
  if (m != null)
    config.camera.pivot = getVerticesCenter(m.vertices)
}

enum class SelectableListType {
  model,
  group
}

data class SelectionEvent(
    val list: SelectableListType,
    val itemIndex: Int
)

//fun loadGeneratedModel(config: ModelViewConfig, renderer: Renderer): Model {
//  val generator = renderer.meshGenerators[config.model]
//  return if (generator != null)
//    generator()
//  else
//    Model(ImmutableMesh(), listOf())
//}

//fun loadExternalMesh(config: ModelViewConfig, renderer: Renderer): Primitives? {
////  val generator = renderer.meshGenerators[config.model]
////  return if (generator != null)
////    null
////  else
////    renderer.meshes[config.model]!!.primitives
//  if (renderer.meshGenerators.containsKey(config.model) || renderer.)
//  return null
//}

class ModelView(val config: ModelViewConfig, val renderer: Renderer, val mousePosition: Vector2) {
  val model: AdvancedModel// = renderer.meshes[config.model]!!
  //  val externalMesh: Primitives? = renderer.meshes[config.model]?.primitives
  val camera = createOrthographicCamera(config.camera)
//  val advancedModel: AdvancedModel?

  init {
    model = AdvancedModel(listOf())
//    val advancedModelGenerator = advancedMeshes(renderer.vertexSchemas)[config.model]
//    advancedModel = if (advancedModelGenerator != null)
//      advancedModelGenerator()
//    else
//      null
//
//    model = if (advancedModel != null && advancedModel.model != null)
//      advancedModel.model!!
//    else
//      loadGeneratedModel(config, renderer)

    // When the model view changes to viewing a different model,
    // the listOld of visible subgroups needs to be reinitialized.
    val m = model.model
    if (m != null && m.groups.size > 0) {
      if (config.visibleGroups.size != m.groups.size)
        config.visibleGroups = m.groups.map { true }.toMutableList()
    } else if (config.visibleGroups.size != model.primitives.size) {
      config.visibleGroups = model.primitives.map { true }.toMutableList()
    }

  }

  fun release() {
//    if (advancedModel != null) {
//      advancedModel.primitives.forEach { it.mesh.dispose() }
//    }
  }

//  fun createLayout(dimensions: Vector2i, state: ModelViewState): ModelLayout {
//    val bounds = Bounds(Vector2i(), dimensions)
//    val initialLengths = listOf(200, null, 300)
//
//    val middle = { b: Bounds -> FlatBox(b, drawScenePanel(config, state, renderer, model, camera)) }
//    val right = { b: Bounds -> FlatBox(b, drawInfoPanel(config, renderer, model, mousePosition)) }
//    val lengths = resolveLengths(dimensions.x, initialLengths)
//    val panelBounds = lengthArranger(horizontalPlane, 0)(bounds, lengths)
//    val boxes = panelBounds.drop(1)
//        .zip(listOf(middle, right), { b, p -> p(b) })
//
////    val (leftBoxes, leftClickBoxes) = drawLeftPanel(renderer.meshes.keys.toList(), config, model, panelBounds[0])
//    val (leftBoxes, leftClickBoxes) = drawLeftPanel(listOf(), config, model, panelBounds[0])
//
//    return ModelLayout(
//        boxes = leftBoxes
//            .plus(boxes),
//        modelPanelBounds = panelBounds[1],
//        clickBoxes = leftClickBoxes
//    )
//  }

  fun onListItemSelection(event: SelectionEvent) {
    when (event.list) {
      SelectableListType.model -> {
//        config.model = renderer.meshes.keys.toList()[event.itemIndex]
        config.visibleGroups = mutableListOf()
      }
      SelectableListType.group -> {
        config.visibleGroups[event.itemIndex] = !config.visibleGroups[event.itemIndex]
      }
    }
  }

  fun updateAnimationOffset(state: ModelViewState, delta: Float): Float {
    val armature = model.armature
    if (armature != null && armature.animations.any())
      return (state.animationElapsedTime + delta * 0.5f) % armature.animations[AnimationId.stand]!!.duration

    return 0f
  }

  fun updateState(layout: ModelLayout, input: LabCommandState, state: ModelViewState, delta: Float): ModelViewState {
    val commands = input.commands

    val mouseRotateSpeedZ = 2.5f
    val mouseRotateSpeedY = 1.5f

    val keyboardRotateSpeedZ = 1.5f
    val keyboardRotateSpeedY = 0.1f

    if (isActive(commands, LabCommandType.select)) {
      val clickBox = filterMouseOverBoxes(layout.clickBoxes, mousePosition.toVector2i())
      if (clickBox != null) {
        onListItemSelection(clickBox.value)
      } else {
        val m = model.model
        if (m != null)
          trySelect(config, camera, m, mousePosition, layout.modelPanelBounds)
      }
    }

    if (isActive(commands, LabCommandType.rotate)) {
      config.camera.rotationZ -= mouseRotateSpeedZ * delta * Math.min(input.mouseOffset.x, 3f)
//      config.camera.rotationY -= mouseRotateSpeedY * delta * Math.min(input.mouseOffset.y, 3f)
    }

    if (isActive(commands, LabCommandType.rotateLeft)) {
      config.camera.rotationZ -= keyboardRotateSpeedZ * delta
    }

    if (isActive(commands, LabCommandType.rotateRight)) {
      config.camera.rotationZ += keyboardRotateSpeedZ * delta
    }

    if (isActive(commands, LabCommandType.pan) && (input.mouseOffset.x != 0f || input.mouseOffset.y != 0f)) {
      val offset = Vector3(0f, input.mouseOffset.x.toFloat(), input.mouseOffset.y.toFloat())
      config.camera.pivot += camera.orientation * offset * delta * config.camera.zoom * 0.14f
    }

    if (isActive(commands, LabCommandType.zoomIn)) {
      config.camera.zoom -= 20 * delta * getCommand(commands, LabCommandType.zoomIn).value
    } else if (isActive(commands, LabCommandType.zoomOut)) {
      config.camera.zoom += 20 * delta * getCommand(commands, LabCommandType.zoomOut).value
    }

//    config.camera.rotationY = Math.min(1.55f, Math.max(-(Pi / 2 - 0.01f), config.camera.rotationY))
    val maxY = Pi / 2f - 0.01f
    config.camera.rotationY = minMax(config.camera.rotationY, -maxY, maxY)
    config.camera.rotationZ = tightenRotation(config.camera.rotationZ)

    return state.copy(animationElapsedTime = updateAnimationOffset(state, delta))
  }

  fun getCommands(): LabCommandMap = mapOf(
//      LabCommandType.rotateLeft to { c -> config.camera.rotationZ -= rotateSpeedZ * c.value },
//      LabCommandType.rotateRight to { c -> config.camera.rotationZ += rotateSpeedZ * c.value },
//      LabCommandType.rotateUp to { c -> config.camera.rotationY += rotateSpeedY * c.value },
//      LabCommandType.rotateDown to { c -> config.camera.rotationY -= rotateSpeedY * c.value },

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
        selectEdgeLoop(config, model.model!!)
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
