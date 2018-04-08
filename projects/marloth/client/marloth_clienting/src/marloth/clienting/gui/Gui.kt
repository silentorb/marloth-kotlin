package marloth.clienting.gui

import mythic.bloom.*
import mythic.drawing.Canvas
import mythic.drawing.grayTone
import mythic.glowing.globalState
import mythic.spatial.Vector2
import mythic.spatial.Vector4
import mythic.typography.TextConfiguration
import org.joml.plus
import rendering.SceneRenderer

fun drawBackground(backgroundColor: Vector4): Depiction = { b: Bounds, canvas: Canvas ->
  globalState.depthEnabled = false
  drawFill(b, canvas, backgroundColor)
  drawBorder(b, canvas, Vector4(0f, 0f, 0f, 1f))
}

val menuBackground: Depiction = drawBackground(grayTone(0.5f))

fun createCenteredBounds(dimensions: Vector2, width: Measurement, height: Measurement): Bounds {
  val resolved = Vector2(
      resolveMeasurement(dimensions, horizontalPlane, width)!!,
      resolveMeasurement(dimensions, verticalPlane, height)!!
  )
  val left = centeredPosition(horizontalPlane, dimensions, resolved.x)
  val top = centeredPosition(verticalPlane, dimensions, resolved.y)
  return Bounds(Vector2(left, top), resolved)
}

fun createMenuLayout(bounds: Bounds): Layout {
//  val panels = listOf(
//      Pair(Measurement(Measurements.pixel, 200f), drawSidePanel()),
//      Pair(Measurement(Measurements.stretch, 0f), drawScenePanel(config, renderer, model, camera)),
//      Pair(Measurement(Measurements.pixel, 300f), drawInfoPanel(config, renderer, model, mousePosition))
//  )
//  val dimensions2 = Vector2(dimensions.x.toFloat(), dimensions.y.toFloat())
//  val boxes = arrangeList(horizontalArrangement, panels, dimensions2)

  return Layout(
      listOf(Box(createCenteredBounds(bounds.dimensions, Measurement(100f), Measurement(100f)), menuBackground))
          .map(applyBounds(bounds))
  )
}

fun renderMenus(bounds: Bounds, canvas: Canvas) {
  val layout = createMenuLayout(bounds)
  renderLayout(layout, canvas)
}

fun renderGui(renderer: SceneRenderer, bounds: Bounds, canvas: Canvas, state: MenuState) {
  canvas.drawText(TextConfiguration("Testing",
      renderer.renderer.fonts[0], 12f, bounds.position + Vector2(10f, 10f), Vector4(1f)))

  if (state.isVisible)
    renderMenus(bounds, canvas)
}
