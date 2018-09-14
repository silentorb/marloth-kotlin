package lab.views.model

import lab.utility.black
import lab.utility.panelColor
import lab.utility.sceneBackgroundColor
import lab.views.shared.SelectableItem
import lab.views.shared.drawSelectableEnumList
import lab.views.shared.drawSelectableList
import mythic.bloom.Bounds
import mythic.bloom.Box
import mythic.bloom.ClickBox
import mythic.bloom.Depiction
import mythic.drawing.Canvas
import mythic.spatial.Vector2
import mythic.spatial.toString
import mythic.typography.TextStyle
import org.joml.Vector2i
import org.joml.plus
import rendering.AdvancedModel
import rendering.MeshType
import rendering.Model
import rendering.Renderer
import rendering.meshes.Primitives
import scenery.Camera

fun drawScenePanel(config: ModelViewConfig, state: ModelViewState, renderer: Renderer, model: AdvancedModel, camera: Camera): Depiction = { b: Bounds, canvas: Canvas ->
  drawBackground(sceneBackgroundColor)(b, canvas)
  drawModelPreview(config, state, renderer, b, camera, model)
}

fun drawSidePanel() = drawBackground(panelColor)
fun drawInfoPanel(config: ModelViewConfig, renderer: Renderer, model: AdvancedModel,
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
/*
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
*/
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

fun drawLeftPanel(meshTypes: List<MeshType>, config: ModelViewConfig, model: AdvancedModel,
                  bounds: Bounds): Pair<List<Box>, List<ClickBox<SelectionEvent>>> {
  val gap = 2f
  val halfGap = gap / 2
  val halfDimensions = Vector2(bounds.dimensions.x, bounds.dimensions.y / 2 - halfGap)
  val modelList = drawSelectableEnumList(meshTypes, config.model, Bounds(bounds.position, halfDimensions))
  val meshGroups = if (model.primitives.size > 0)
    model.primitives.mapIndexed { index, it ->
      SelectableItem(it.name, config.visibleGroups[index])
    }
  else
    model.primitives.mapIndexed { i, it -> SelectableItem(it.name, config.visibleGroups[i]) }

  val groupListBounds = Bounds(bounds.position + Vector2(0f, bounds.dimensions.y / 2 + halfGap), halfDimensions)
  val groupList = drawSelectableList(meshGroups, SelectableListType.group, groupListBounds)
  return Pair(modelList.first.plus(groupList.first), modelList.second.plus(groupList.second))
}