package lab.views

import haft.isActive
import lab.LabCommandType
import lab.views.model.SelectionEvent
import lab.views.shared.SelectionResult
import lab.views.shared.drawSelectableEnumList
import mythic.bloom.*
import mythic.drawing.Canvas
import mythic.spatial.Vector2
import mythic.spatial.toVector2
import mythic.spatial.toVector2i
import org.joml.Vector2i
import rendering.Renderer
import scenery.Textures

data class TextureViewConfig(
    var texture: Textures = Textures.checkers
)

fun drawTextureView(renderer: Renderer, config: TextureViewConfig, bounds: Bounds, canvas: Canvas) {
//  val texture = textureGenerators[config.texture]!!(1f)
  val texture = renderer.mappedTextures[config.texture]!!
  val length = Math.min(bounds.dimensions.x, bounds.dimensions.y).toFloat()
  val repeat = 2f
  canvas.drawDynamicImage(bounds.position.toVector2(), Vector2(length, length), canvas.image(texture), listOf(
      0f, 1f, 0f, repeat,
      0f, 0f, 0f, 0f,
      1f, 0f, repeat, 0f,
      1f, 1f, repeat, repeat
  ))
//  texture.dispose()
}

private fun drawLeftPanel(textures: List<Textures>, config: TextureViewConfig, bounds: Bounds): SelectionResult {
  return drawSelectableEnumList(textures, config.texture, bounds)
}

data class TextureViewLayout(
    val boxes: List<Box>,
    val clickBoxes: List<ClickBox<SelectionEvent>>
)

class TextureView {

  fun createLayout(renderer: Renderer, config: TextureViewConfig, dimensions: Vector2i): TextureViewLayout {
//    val draw = { b: Bounds, c: Canvas -> drawBorder(b, c, Vector4(0f, 0f, 1f, 1f)) }
//
//    val panels = listOf(
//        Pair(Measurement(Measurements.pixel, 200f), draw),
//        Pair(Measurement(Measurements.stretch, 0f), { b: Bounds, c: Canvas ->
//          drawTextureView(renderer, config, b, c)
//          draw(b, c)
//        })
//    )
//    val dimensions2 = Vector2(dimensions.x.toFloat(), dimensions.y.toFloat())
//    val boxes = arrangeMeasuredList(measuredHorizontalArrangement, panels, dimensions2)
//
//    return boxes
    val bounds = Bounds(Vector2i(), dimensions)
    val initialLengths = listOf(200, null)

    val middle = { b: Bounds -> Box(b, { b, c -> drawTextureView(renderer, config, b, c) }) }
    val lengths = resolveLengths(dimensions.x, initialLengths)
    val panelBounds = arrangeHorizontal(0)(bounds, lengths)
    val boxes = panelBounds.drop(1)
        .zip(listOf(middle), { b, p -> p(b) })

    val left = drawLeftPanel(renderer.mappedTextures.keys.toList(), config, panelBounds[0])
    val (leftBoxes, leftClickBoxes) = left

    return TextureViewLayout(
        boxes = leftBoxes
            .plus(boxes),
        clickBoxes = leftClickBoxes
    )
  }
}

fun onListItemSelection(event: SelectionEvent, config: TextureViewConfig, renderer: Renderer) {
  config.texture = renderer.mappedTextures.keys.toList()[event.itemIndex]
}

fun updateTextureState(layout: TextureViewLayout, input: LabCommandState, config: TextureViewConfig, renderer: Renderer) {
  val commands = input.commands

  if (isActive(commands, LabCommandType.select)) {
    val clickBox = filterMouseOverBoxes(layout.clickBoxes, input.mousePosition.toVector2i())
    if (clickBox != null) {
      onListItemSelection(clickBox.value, config, renderer)
    } else {
    }
  }
}
