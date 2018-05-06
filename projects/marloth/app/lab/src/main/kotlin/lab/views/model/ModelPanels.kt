package lab.views.model

import lab.utility.black
import lab.utility.panelColor
import lab.utility.sceneBackgroundColor
import mythic.bloom.*
import mythic.drawing.Canvas
import mythic.drawing.grayTone
import mythic.glowing.globalState
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import mythic.typography.TextConfiguration
import mythic.typography.TextStyle
import mythic.typography.calculateTextDimensions
import org.joml.Vector2i
import org.joml.plus
import rendering.MeshType
import rendering.Model
import rendering.Renderer
import rendering.meshes.ModelElements
import scenery.Camera
import java.text.DecimalFormat

fun drawScenePanel(config: ModelViewConfig, renderer: Renderer, model: Model, camera: Camera, modelElements: ModelElements?): Depiction = { b: Bounds, canvas: Canvas ->
  drawBackground(sceneBackgroundColor)(b, canvas)
  drawModelPreview(config, renderer, b, camera, model, modelElements)
}

val decimalFormat = DecimalFormat("#.#####")
fun toString(vector: Vector3) =
    decimalFormat.format(vector.x) + ", " + decimalFormat.format(vector.y) + ", " + decimalFormat.format(vector.z)

fun drawSidePanel() = drawBackground(panelColor)
fun drawInfoPanel(config: ModelViewConfig, renderer: Renderer, model: Model,
                  mousePosition: Vector2i): Depiction = { bounds: Bounds, canvas: Canvas ->
  drawSidePanel()(bounds, canvas)
  var row = 1

  val textStyle = TextStyle(canvas.fonts[0], 12f, black)
  fun drawText(content: String) {
    canvas.drawText(content, bounds.position + Vector2(5f, 5f + row++ * 20f), textStyle)
  }
  drawText("Mouse: " + mousePosition.x.toString() + ", " + mousePosition.y.toString())
//  canvas.drawText(TextConfiguration("Mouse: " + mousePosition.x.toString() + ", " + mousePosition.y.toString(),
//      renderer.fonts[0], 12f, bounds.position + Vector2(5f, 5f), black))

  if (config.selection.size > 0) {
    when (config.componentMode) {
      ComponentMode.vertices -> {
        val vertices = model.vertices
        if (vertices.size > config.selection.first()) {
          val vertex = vertices[config.selection.first()]
          drawText(toString(vertex) + " " + vertex.hashCode())
        }
      }

      ComponentMode.faces -> {
        val faces = model.mesh.faces
        val first = config.selection.first()
        if (faces.size > first) {
          val face = faces[first]
          drawText("Face " + first + ":")
          for (vertex in face.vertices) {
            drawText(" " + toString(vertex))
          }
        }
      }
    }
//    canvas.drawText(TextConfiguration(toString(vertices[config.vertexSelection.first()]),
//        renderer.fonts[0], 12f, bounds.position + Vector2(5f, 25f), black))
  }

  drawText("rotationY: " + config.camera.rotationY)
  drawText("rotationZ: " + config.camera.rotationZ)
  drawText("pivot: " + toString(config.camera.pivot))

  drawText("tempStart: " + toString(config.tempStart))
  drawText("tempEnd: " + toString(config.tempEnd))
//  canvas.drawText(TextConfiguration("ts: " + config.tempStart.x.toString() + ", " + config.tempStart.y.toString() + ", " + config.tempStart.z.toString(),
//      renderer.fonts[0], 12f, bounds.position + Vector2(5f, 25f), black))
//  canvas.drawText(TextConfiguration("te: " + config.tempEnd.x.toString() + ", " + config.tempEnd.y.toString() + ", " + config.tempEnd.z.toString(),
//      renderer.fonts[0], 12f, bounds.position + Vector2(5f, 45f), black))
}

fun drawListItem(text: String, isSelected: Boolean): Depiction = { bounds: Bounds, canvas: Canvas ->
  globalState.depthEnabled = false
  drawFill(bounds, canvas, grayTone(0.5f))
  val style = if (isSelected)
    Pair(12f, LineStyle(Vector4(1f), 2f))
  else
    Pair(12f, LineStyle(Vector4(0f, 0f, 0f, 1f), 1f))

  drawBorder(bounds, canvas, style.second)

  val blackStyle = TextStyle(canvas.fonts[0], style.first, Vector4(0f, 0f, 0f, 1f))
  val textConfig = TextConfiguration(text, bounds.position, blackStyle)
  val textDimensions = calculateTextDimensions(textConfig)
  val centered = centeredPosition(bounds, textDimensions)
  val position = Vector2(bounds.position.x + 10f, centered.y)
  canvas.drawText(text, position, blackStyle)
}

data class SelectableItem(
    val name: String,
    val isSelected: Boolean
)

fun drawSelectableList(items: List<SelectableItem>, list: SelectableListType, bounds: Bounds): Pair<List<Box>, List<ClickBox<SelectionEvent>>> {
  val padding = Vector2(10f)
  val itemHeight = 30f

  val partialBoxes = items
      .map { PartialBox(itemHeight, drawListItem(it.name, it.isSelected)) }

  val buttonBoxes = arrangeList(verticalArrangement(padding), partialBoxes, bounds)
  val boxes = listOf(
      Box(bounds, drawSidePanel())
  )
      .plus(buttonBoxes)
  return Pair(boxes, buttonBoxes.mapIndexed { i, b -> ClickBox(b.bounds, SelectionEvent(list, i)) })
}

fun drawLeftPanel(meshTypes: List<MeshType>, config: ModelViewConfig, model: Model) = { bounds: Bounds ->
  val gap = 2f
  val halfGap = gap / 2
  val halfDimensions = Vector2(bounds.dimensions.x, bounds.dimensions.y / 2 - halfGap)
  val focusIndex = meshTypes.indexOf(config.model)
  val modelItems = meshTypes.mapIndexed { index, it ->
    SelectableItem(it.name, focusIndex == index)
  }

  val modelList = drawSelectableList(modelItems, SelectableListType.model, Bounds(bounds.position, halfDimensions))

  val meshGroups = model.groups.mapIndexed { index, it ->
    SelectableItem(it.name, config.visibleGroups[index])
  }

  val groupListBounds = Bounds(bounds.position + Vector2(0f, bounds.dimensions.y / 2 + halfGap), halfDimensions)
  val groupList = drawSelectableList(meshGroups, SelectableListType.group, groupListBounds)
  Pair(modelList.first.plus(groupList.first), modelList.second.plus(groupList.second))

//  val padding = Vector2(10f)
//  val itemHeight = 30f
//  val focusIndex = meshTypes.indexOf(config.model)
//
//  val items = meshTypes
//      .map { it.name }
//      .mapIndexed { index, it -> PartialBox(itemHeight, drawListItem(it, focusIndex == index)) }
//
//  val buttonBoxes = arrangeList(verticalArrangement(padding), items, bounds)
//  val boxes = listOf(
//      Box(bounds, drawSidePanel())
//  )
//      .plus(buttonBoxes)
//  Pair(boxes, buttonBoxes.mapIndexed { i, b -> ClickBox(b.bounds, SelectionEvent(i)) })
}